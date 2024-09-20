package `in`.francl.cam.infrastructure.monitoring.instrumentation

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class TaskExecutionMeasurement(
    private val meterRegistry: MeterRegistry,
) : Measurable {

    override fun measure(tags: HashMap<String, String>, duration: Duration) {
        Timer.builder("task_execution")
            .tags(tags.map { (k, v) -> Tag.of(k, v) })
            .publishPercentiles(0.5, 0.90, 0.95, 0.99)
            .register(meterRegistry)
            .record(duration.toJavaDuration())
    }

}
