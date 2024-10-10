package `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.handler

import `in`.francl.cam.domain.model.Task
import `in`.francl.cam.domain.port.inbound.task.TaskHandler
import `in`.francl.cam.domain.port.outbound.task.TaskManager
import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.dto.CamundaExternalTask
import `in`.francl.cam.infrastructure.monitoring.measurement.Measurable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import net.logstash.logback.marker.Markers
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.client.task.ExternalTaskHandler
import org.camunda.bpm.client.task.ExternalTaskService
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.Instant
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class DefaultExternalTaskHandler(
    private val handler: TaskHandler,
    private val taskManagerDelegate: (ExternalTaskService) -> TaskManager,
    private val taskAcquireMeasurement: Measurable,
) : ExternalTaskHandler {
    override fun execute(externalTask: ExternalTask, externalTaskService: ExternalTaskService) {
        val task = CamundaExternalTask(externalTask, Instant.now()).toTask()
        val service = taskManagerDelegate(externalTaskService)

        measureAcquireTime(task)

        hashMapOf(
            "id" to task.id,
            "retries" to task.retries?.toString(),
            "workerId" to task.workerId,
            "topicName" to task.topicName,
            "activityId" to task.activityId,
            "executionId" to task.executionId,
            "businessKey" to task.businessKey,
            "correlationId" to task.variables["correlationId"]?.toString(),
            "processInstanceId" to task.processInstanceId,
            "activityInstanceId" to task.activityInstanceId,
            "processDefinitionId" to task.processDefinitionId,
            "processDefinitionKey" to task.processDefinitionKey,
            "processDefinitionVersionTag" to task.processDefinitionVersionTag,
        ).forEach(MDC::put)

        val taskMarker = Markers.append("task", task.copy(originalTask = null))!!
        logger.info(taskMarker, "Task handling")

        CoroutineScope(Dispatchers.IO).launch(MDCContext()) {
            logger.info("Task executing")
            handler.execute(task, service)
            logger.info("Task executed")
        }

        logger.info("Task handled")
        MDC.clear()
    }

    private fun measureAcquireTime(task: Task) {
        task.createTime?.also {
            val acquireTime = (task.acquireAt.toEpochMilli() - it.time).toDuration(DurationUnit.MILLISECONDS)
            val tags: HashMap<String, String> = hashMapOf(
                "topicName" to task.topicName,
                "activityId" to task.activityId,
                "processDefinitionKey" to task.processDefinitionKey,
            )
            taskAcquireMeasurement.measure(tags, acquireTime)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultExternalTaskHandler::class.java)!!
    }
}