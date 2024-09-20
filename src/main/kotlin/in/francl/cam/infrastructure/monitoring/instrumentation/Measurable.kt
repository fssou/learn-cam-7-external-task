package `in`.francl.cam.infrastructure.monitoring.instrumentation

import kotlin.time.Duration

interface Measurable {
    fun measure(tags: HashMap<String, String>, duration: Duration)
}
