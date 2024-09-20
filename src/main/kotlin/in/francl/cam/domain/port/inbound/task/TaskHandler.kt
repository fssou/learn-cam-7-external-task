package `in`.francl.cam.domain.port.inbound.task

import `in`.francl.cam.domain.model.Task
import `in`.francl.cam.domain.port.outbound.task.TaskManager

interface TaskHandler {
    suspend fun execute(task: Task, service: TaskManager)
}
