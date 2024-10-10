package `in`.francl.cam.application.handler

import `in`.francl.cam.domain.port.inbound.task.TaskHandler

class TaskHandlerRegistry {
    private val taskHandlers = mutableSetOf<Pair<TaskHandler, TaskHandlerConfig>>()

    fun register(handler: TaskHandler, block: TaskHandlerConfig.() -> Unit) {
        val config = TaskHandlerConfig().apply(block)
        taskHandlers.add(handler to config)
    }

    val handlers: Set<Pair<TaskHandler, TaskHandlerConfig>>
        get() = taskHandlers
}