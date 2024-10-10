package `in`.francl.cam.application.handler

import `in`.francl.cam.application.error.BusinessServiceError
import `in`.francl.cam.application.error.FailureServiceError
import `in`.francl.cam.domain.model.Task
import `in`.francl.cam.application.port.inbound.task.TaskHandler
import `in`.francl.cam.application.port.outbound.task.TaskManager
import net.logstash.logback.marker.Markers
import org.slf4j.LoggerFactory

class DefaultTaskHandler(
    private val performable: Performable,
) : `in`.francl.cam.application.port.inbound.task.TaskHandler {

    override suspend fun execute(task: Task, service: `in`.francl.cam.application.port.outbound.task.TaskManager) {
        runCatching {
            logger.info("Task performing")
            performable
                .perform(task, service)
                .onRight { taskResult ->
                    logger.info(
                        Markers.append("taskResult", taskResult),
                        "Task performed successfully",
                    )
                    service.success(
                        task,
                        mapOf(),
                        hashMapOf("output" to taskResult.variables)
                    )
                        .onFailure { throwable ->
                            logger.error(
                                Markers.append("severity", 0),
                                "Error on handling success",
                                throwable
                            )
                            service.failure(
                                task,
                                "[INTERNAL] ${throwable.message}",
                                throwable.stackTraceToString(),
                                0,
                                0,
                                mapOf(),
                                hashMapOf(),
                            )
                                .onFailure { throwable ->
                                    logger.error(
                                        Markers.append("severity", 0),
                                        "Error on handling failure",
                                        throwable
                                    )
                                }
                        }
                }
                .onLeft { error ->
                    when (error) {
                        is FailureServiceError -> {
                            logger.info(
                                Markers.append("failure", error),
                                "A failure has occurred"
                            )
                            // Na próxima tentativa o valor de error.retries não é mais considerado.
                            val retries = task.retries?.dec() ?: error.retries
                            service.failure(
                                task,
                                "[${error.code}] ${error.message}",
                                error.details,
                                if (retries > 0) retries else 0,
                                error.retryTimeout,
                                mapOf(),
                                hashMapOf("output" to error.variables),
                            )
                        }
                        is BusinessServiceError -> {
                            logger.info(
                                Markers.append("error", error),
                                "A business error has occurred"
                            )
                            service.error(
                                task,
                                error.code,
                                error.message,
                                hashMapOf("output" to error.variables),
                            )
                        }
                        else -> {
                            Result.failure(Exception("Unknown error type", error.cause))
                        }
                    }
                        .onFailure { throwable ->
                            logger.error(
                                Markers.append("severity", 0),
                                "Exception on handling error or failure",
                                throwable
                            )
                            service.failure(
                                task,
                                "[${error.code}] ${error.message}",
                                error.details,
                                0,
                                0,
                                mapOf(),
                                hashMapOf(),
                            )
                        }
                }
        }
            .onFailure { throwable ->
                logger.error(
                    Markers.append("severity", 0),
                    "Error performing task",
                    throwable
                )
                service.failure(
                    task,
                    "[INTERNAL] ${throwable.message}",
                    throwable.stackTraceToString(),
                    0,
                    0,
                    mapOf(),
                    hashMapOf(),
                )
            }
        logger.info("Task performed")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultTaskHandler::class.java)!!
    }

}
