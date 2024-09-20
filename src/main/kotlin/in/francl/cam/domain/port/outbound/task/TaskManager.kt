package `in`.francl.cam.domain.port.outbound.task

import `in`.francl.cam.domain.model.Task

interface TaskManager : TaskLocker, TaskResultSender {
    suspend fun setVariables(task: Task, variables: Map<String, Any?>) : Result<Unit>
}