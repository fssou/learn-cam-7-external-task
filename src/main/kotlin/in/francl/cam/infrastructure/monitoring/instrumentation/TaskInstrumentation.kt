package `in`.francl.cam.infrastructure.monitoring.instrumentation

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.Instant
import java.util.concurrent.TimeUnit

class TaskInstrumentation(
    private val meterRegistry: MeterRegistry,
) {

    fun measureTaskHandler(
        task: `in`.francl.cam.domain.ports.inbound.task.Task,
        handlerResult: String,
        error: Throwable? = null,
    ) {
        val executionTime = Instant.now().minusMillis(task.acquireAt.toEpochMilli())
        Timer.builder("task_execution")
            .tag("topicName", task.topicName)
            .tag("activityId", task.activityId)
            .tag("processDefinitionKey", task.processDefinitionKey)
            .tag("error", error?.javaClass?.simpleName ?: "none")
            .tag("result", handlerResult)
            .publishPercentiles(0.5, 0.90, 0.95, 0.99)
            .register(meterRegistry)
            .record(executionTime.toEpochMilli(), TimeUnit.MILLISECONDS)

    }

    fun measureTaskAcquireTime(
        task: `in`.francl.cam.domain.ports.inbound.task.Task,
    ) {
        task.createTime?.let { createTime ->
            val acquireTime = task.acquireAt.minusMillis(createTime.time)
            Timer.builder("task_acquisition")
                .tag("topicName", task.topicName)
                .tag("activityId", task.activityId)
                .tag("processDefinitionKey", task.processDefinitionKey)
                .publishPercentiles(0.5, 0.90, 0.95, 0.99)
                .register(meterRegistry)
                .record(acquireTime.toEpochMilli(), TimeUnit.MILLISECONDS)
        }
    }

}
