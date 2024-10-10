package `in`.francl.cam.application.port.inbound.task

import `in`.francl.cam.domain.model.Task
import `in`.francl.cam.application.port.outbound.task.TaskManager

interface TaskHandler {
    suspend fun execute(task: Task, service: `in`.francl.cam.application.port.outbound.task.TaskManager)
}
