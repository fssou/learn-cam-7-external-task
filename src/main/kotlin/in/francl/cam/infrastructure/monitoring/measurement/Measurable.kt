package `in`.francl.cam.infrastructure.monitoring.measurement

import kotlin.time.Duration

interface Measurable {
    fun measure(tags: HashMap<String, String>, duration: Duration)
}
