package `in`.francl.cam.domain.ports.outbound.task

import `in`.francl.cam.domain.ports.inbound.task.Task

interface TaskManager : TaskLocker, TaskResultSender {
    suspend fun setVariables(task: Task, variables: Map<String, Any?>)
}