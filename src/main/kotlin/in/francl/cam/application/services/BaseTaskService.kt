package `in`.francl.cam.application.services

import arrow.core.Either
import `in`.francl.cam.application.dto.TaskResult
import `in`.francl.cam.application.errors.BusinessServiceError
import `in`.francl.cam.application.errors.FailureServiceError
import `in`.francl.cam.application.errors.ServiceError
import `in`.francl.cam.domain.ports.inbound.task.Task
import `in`.francl.cam.domain.ports.inbound.task.TaskHandler
import `in`.francl.cam.domain.ports.outbound.task.TaskLocker
import `in`.francl.cam.domain.ports.outbound.task.TaskManager
import net.logstash.logback.marker.Markers
import org.slf4j.LoggerFactory

abstract class BaseTaskService : TaskHandler {
    override suspend fun execute(task: Task, service: TaskManager) {
        runCatching {
            logger.info(Markers.append("task", task), "Task performing")
            perform(task, service)
                .onRight {
                    runCatching {
                        logger.info(
                            Markers.append("taskResult", it),
                            "Task performed successfully",
                        )
                        service.success(
                            task,
                            mapOf(),
                            hashMapOf("output" to it.output)
                        )
                    }.onFailure {
                        logger.error(
                            Markers.append("severity", 0),
                            "Error on handling success",
                            it
                        )
                    }
                }
                .onLeft { error ->
                    runCatching {
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
                        }
                    }
                        .onFailure {
                            logger.error(
                                Markers.append("severity", 0),
                                "Exception on handling error or failure",
                                it
                            )
                        }
                }
        }
            .onFailure {
                logger.error(
                    Markers.append("severity", 0),
                    "Error performing task",
                    it
                )
            }
        logger.info("Task performed")
    }

    protected abstract suspend fun perform(task: Task, taskLocker: TaskLocker): Either<ServiceError, TaskResult>

    companion object {
        private val logger = LoggerFactory.getLogger(BaseTaskService::class.java)!!
    }

}
