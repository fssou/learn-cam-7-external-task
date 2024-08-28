package `in`.francl.cam.infrastructure.adapters.outbound.camunda

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import `in`.francl.cam.domain.ports.inbound.task.Task
import `in`.francl.cam.domain.ports.outbound.task.TaskManager
import `in`.francl.cam.infrastructure.monitoring.instrumentation.TaskInstrumentation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.camunda.bpm.client.task.ExternalTask
import org.camunda.bpm.client.task.ExternalTaskService
import org.slf4j.LoggerFactory

/**
 * TODO: Tratar exceções das chamadas ao camunda
 * Adapter to handle the communication with the external task service
 */
class ExternalTaskServiceAdapter(
    private val externalService: ExternalTaskService,
    private val taskInstrumentation: TaskInstrumentation,
) : TaskManager {
    private val objectMapper = ObjectMapper()
    private val mapStringAny: JavaType = objectMapper.typeFactory
        .constructMapType(Map::class.java, String::class.java, Any::class.java)

    override suspend fun success(task: Task, variables: Map<String, Any?>, localVariables: Map<String, Any?>) {
        withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching {
                val variablesConverted = objectMapper.convertValue<Map<String, Any?>>(variables, mapStringAny)
                val localVariablesConverted = objectMapper.convertValue<Map<String, Any?>>(localVariables, mapStringAny)
                externalService.complete(
                    task.getOriginalTask<ExternalTask>(),
                    variablesConverted,
                    localVariablesConverted
                )
            }.onSuccess {
                taskInstrumentation.measureTaskHandler(task, "success")
            }.onFailure { error ->
                taskInstrumentation.measureTaskHandler(task, "success", error)
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
    ) {
        withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching {
                val variablesConverted = objectMapper.convertValue<Map<String, Any?>>(variables, mapStringAny)
                val localVariablesConverted = objectMapper.convertValue<Map<String, Any?>>(localVariables, mapStringAny)
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
                .onSuccess { taskInstrumentation.measureTaskHandler(task, "failure") }
                .onFailure { error -> taskInstrumentation.measureTaskHandler(task, "failure", error) }
        }
    }

    override suspend fun error(task: Task, code: String, message: String, variables: Map<String, Any?>) {
        withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching {
                val variablesConverted = objectMapper.convertValue<Map<String, Any?>>(variables, mapStringAny)
                externalService.handleBpmnError(task.id, code, message, variablesConverted)
            }
                .onSuccess { taskInstrumentation.measureTaskHandler(task, "error") }
                .onFailure { error -> taskInstrumentation.measureTaskHandler(task, "error", error) }
        }
    }

    override suspend fun lock(task: Task, lockDuration: Long) {
        withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching { externalService.lock(task.id, lockDuration) }
        }
    }

    override suspend fun unlock(task: Task) {
        withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching { externalService.unlock(task.getOriginalTask()) }
        }
    }

    override suspend fun extendLock(task: Task, newDuration: Long) {
        withContext(Dispatchers.IO.plus(MDCContext())) {
            runCatching { externalService.extendLock(task.id, newDuration) }
        }
    }

    override suspend fun setVariables(task: Task, variables: Map<String, Any?>) {
        withContext(Dispatchers.IO.plus(MDCContext())) {
            val variablesConverted = objectMapper.convertValue<Map<String, Any?>>(variables, mapStringAny)
            runCatching { externalService.setVariables(task.getOriginalTask<ExternalTask>(), variablesConverted) }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExternalTaskServiceAdapter::class.java)!!
    }

}
