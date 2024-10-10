package `in`.francl.cam.application.service.httpcodes

import arrow.core.Either
import `in`.francl.cam.application.dto.TaskResult
import `in`.francl.cam.application.error.ServiceError
import `in`.francl.cam.application.handler.Performable
import `in`.francl.cam.domain.model.Task
import `in`.francl.cam.application.port.outbound.task.TaskLocker

class HttpCodesPerformable : Performable {
    override suspend fun perform(task: Task, taskLocker: `in`.francl.cam.application.port.outbound.task.TaskLocker): Either<ServiceError, TaskResult> {
        TODO("Not yet implemented")
    }

}