package `in`.francl.cam.application.port.outbound.task

import `in`.francl.cam.domain.model.Task

interface TaskManager : `in`.francl.cam.application.port.outbound.task.TaskLocker,
    `in`.francl.cam.application.port.outbound.task.TaskResultSender {
    suspend fun setVariables(task: Task, variables: Map<String, Any?>) : Result<Unit>
}