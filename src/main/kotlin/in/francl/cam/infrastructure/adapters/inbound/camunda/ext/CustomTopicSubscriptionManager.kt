package `in`.francl.cam.infrastructure.adapters.inbound.camunda.ext

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import org.camunda.bpm.client.backoff.ErrorAwareBackoffStrategy
import org.camunda.bpm.client.impl.EngineClient
import org.camunda.bpm.client.impl.EngineClientException
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.client.topic.TopicSubscription
import org.camunda.bpm.client.topic.impl.TopicSubscriptionManager
import org.camunda.bpm.client.topic.impl.dto.FetchAndLockResponseDto
import org.camunda.bpm.client.topic.impl.dto.TopicRequestDto
import org.camunda.bpm.client.variable.impl.TypedValues
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CustomTopicSubscriptionManager(
    engineClient: EngineClient,
    typedValues: TypedValues,
    clientLockDuration: Long,
) : TopicSubscriptionManager(
    engineClient,
    typedValues,
    clientLockDuration,
) {

    private val acquisitionMonitor = Mutex()
    private var acquisitionJob: Job? = null

    override fun start() {
        if (isRunning.compareAndSet(false, true)) {
            acquisitionJob = CoroutineScope(Dispatchers.Default).launch {
                runAcquisitionLoop()
            }
        }
    }

    override fun stop() {
        if (isRunning.compareAndSet(true, false)) {
            acquisitionJob?.cancel()
        }
    }

    private suspend fun runAcquisitionLoop() {
        while (isRunning.get()) {
            try {
                acquireSuspend()
            } catch (e: Throwable) {
                logger.error("Exception while acquiring tasks", e)
            }
        }
    }

    private suspend fun acquireSuspend() {
        taskTopicRequests.clear()
        externalTaskHandlers.clear()
        subscriptions.forEach(::prepareAcquisition)

        if (taskTopicRequests.isNotEmpty()) {
            val fetchAndLockResponse = fetchAndLockSuspend(taskTopicRequests)
            fetchAndLockResponse.externalTasks.forEach { externalTask ->
                val topicName = externalTask.topicName
                val taskHandler = externalTaskHandlers[topicName]
                if (taskHandler != null) {
                    handleExternalTask(externalTask, taskHandler)
                } else {
                    LOG.taskHandlerIsNull(topicName)
                }
            }
            if (!isBackoffStrategyDisabled.get()) {
                runBackoffStrategySuspend(fetchAndLockResponse)
            }
        }
    }

    private suspend fun fetchAndLockSuspend(subscriptions: List<TopicRequestDto>): FetchAndLockResponseDto {
        val externalTasks: List<ExternalTask>?
        return try {
            logger.debug("Fetch and lock new external tasks for ${subscriptions.size} topics")
            externalTasks = withContext(Dispatchers.IO){
                engineClient.fetchAndLock(subscriptions)
            }
            FetchAndLockResponseDto(externalTasks)
        } catch (ex: EngineClientException) {
            logger.error("Exception while fetching and locking task", ex)
            FetchAndLockResponseDto(LOG.handledEngineClientException("fetching and locking task", ex))
        }
    }

    private suspend fun runBackoffStrategySuspend(fetchAndLockResponse: FetchAndLockResponseDto) {
        try {
            val externalTasks = fetchAndLockResponse.externalTasks
            if (backoffStrategy is ErrorAwareBackoffStrategy) {
                val errorAwareBackoffStrategy = backoffStrategy as ErrorAwareBackoffStrategy
                val exception = fetchAndLockResponse.error
                errorAwareBackoffStrategy.reconfigure(externalTasks, exception)
            } else {
                backoffStrategy?.reconfigure(externalTasks)
            }
            val waitTime = backoffStrategy?.calculateBackoffTime() ?: 0L
            lockAcquisitionMonitor(waitTime)
        } catch (e: Throwable) {
            logger.error("Exception while executing back off strategy method", e)
        }
    }

    private suspend fun lockAcquisitionMonitor(waitTime: Long) {
        if (waitTime > 0 && isRunning.get()) {
            delay(waitTime)
        }
    }

    override fun subscribe(subscription: TopicSubscription) {
        if (!subscriptions.addIfAbsent(subscription)) {
            val topicName = subscription.topicName
            throw LOG.topicNameAlreadySubscribedException(topicName)
        }
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(CustomTopicSubscriptionManager::class.java)
    }

}