package `in`.francl.cam.application.handler

import `in`.francl.cam.application.port.inbound.task.TaskHandler

class TaskHandlerRegistry {
    private val taskHandlers = mutableSetOf<Pair<`in`.francl.cam.application.port.inbound.task.TaskHandler, TaskHandlerConfig>>()

    fun register(handler: `in`.francl.cam.application.port.inbound.task.TaskHandler, block: TaskHandlerConfig.() -> Unit) {
        val config = TaskHandlerConfig().apply(block)
        taskHandlers.add(handler to config)
    }

    val handlers: Set<Pair<`in`.francl.cam.application.port.inbound.task.TaskHandler, TaskHandlerConfig>>
        get() = taskHandlers
}