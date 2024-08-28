package `in`.francl.cam.domain.ports.inbound.task

import `in`.francl.cam.domain.ports.outbound.task.TaskManager

interface TaskHandler {
    suspend fun execute(task: Task, service: TaskManager)
}
