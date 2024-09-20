package `in`.francl.cam.application.handler

import arrow.core.Either
import `in`.francl.cam.application.dto.TaskResult
import `in`.francl.cam.application.error.ServiceError
import `in`.francl.cam.domain.model.Task
import `in`.francl.cam.domain.port.outbound.task.TaskLocker

interface Performable {

    suspend fun perform(task: Task, taskLocker: TaskLocker): Either<ServiceError, TaskResult>

}
