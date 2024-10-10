package `in`.francl.cam.infrastructure.adapter.outbound.http.camunda

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import `in`.francl.cam.domain.model.Task
import `in`.francl.cam.domain.port.outbound.task.TaskManager
import `in`.francl.cam.infrastructure.monitoring.measurement.Measurable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.client.task.ExternalTaskService
import org.slf4j.LoggerFactory
import java.time.Instant
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Adapter to handle the communication with the external task handler
 */
class ExternalTaskServiceAdapter(
    private val externalService: ExternalTaskService,
    private val taskExecutionMeasurement: Measurable,
) : TaskManager {

    override suspend fun success(task: Task, variables: Map<String, Any?>, localVariables: Map<String, Any?>) : Result<Unit> {
        return withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching {
                val variablesConverted = convertVariables(variables)
                val localVariablesConverted = convertVariables(localVariables)
                externalService.complete(
                    task.id,
                    variablesConverted,
                    localVariablesConverted
                )
            }.onSuccess {
                measure(task, HandlerType.COMPLETE)
            }.onFailure { error ->
                measure(task, HandlerType.COMPLETE, error)
            }
        }
    }

    override suspend fun failure(
        task: Task,
        errorMessage: String,
        errorDetails: String,
        retries: Int,
        retryTimeout: Long,
        variables: Map<String, Any?>,
        localVariables: Map<String, Any?>,
    ) : Result<Unit> {
        return withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching {
                val variablesConverted = convertVariables(variables)
                val localVariablesConverted = convertVariables(localVariables)
                externalService.handleFailure(
                    task.id,
                    errorMessage,
                    errorDetails,
                    retries,
                    retryTimeout,
                    variablesConverted,
                    localVariablesConverted,
                )
            }
                .onSuccess { measure(task, HandlerType.FAILURE) }
                .onFailure { error -> measure(task, HandlerType.FAILURE, error) }
        }
    }

    override suspend fun error(task: Task, code: String, message: String, variables: Map<String, Any?>) : Result<Unit> {
        return withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching {
                val variablesConverted = convertVariables(variables)
                externalService.handleBpmnError(task.id, code, message, variablesConverted)
            }
                .onSuccess { measure(task, HandlerType.ERROR) }
                .onFailure { error -> measure(task, HandlerType.ERROR, error) }
        }
    }

    override suspend fun lock(task: Task, lockDuration: Long) : Result<Unit> {
        return withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching { externalService.lock(task.id, lockDuration) }
        }
    }

    override suspend fun unlock(task: Task) : Result<Unit> {
        return withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching { externalService.unlock(task.originalTask as ExternalTask) }
        }
    }

    override suspend fun extendLock(task: Task, newDuration: Long) : Result<Unit> {
        return withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching { externalService.extendLock(task.id, newDuration) }
        }
    }

    override suspend fun setVariables(task: Task, variables: Map<String, Any?>) : Result<Unit> {
        return withContext(Dispatchers.IO.plus(MDCContext())) {
            val variablesConverted = convertVariables(variables)
            runCatching { externalService.setVariables(task.originalTask as ExternalTask, variablesConverted) }
        }
    }

    private fun measure(task: Task, handlerType: HandlerType, error: Throwable? = null) {
        val executionTime = Instant.now()
            .minusMillis(task.acquireAt.toEpochMilli())
            .toEpochMilli()
            .toDuration(DurationUnit.MILLISECONDS)
        val errorClassName = error?.javaClass?.simpleName ?: "none"
        val tags = hashMapOf(
            "topicName" to task.topicName,
            "activityId" to task.activityId,
            "processDefinitionKey" to task.processDefinitionKey,
            "processDefinitionId" to task.processDefinitionId,
            "error" to errorClassName,
            "handler" to handlerType.name,
        )
        taskExecutionMeasurement.measure(tags, executionTime)
    }

    private fun convertVariables(variables: Map<String, Any?>): Map<String, Any?> {
        val mapper = jacksonObjectMapper()
        val mapStringAny: JavaType = mapper.typeFactory
            .constructMapType(HashMap::class.java, String::class.java, Any::class.java)
        return mapper.convertValue(variables, mapStringAny)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExternalTaskServiceAdapter::class.java)!!
    }

    private enum class HandlerType {
        COMPLETE,
        FAILURE,
        ERROR,
    }

}
