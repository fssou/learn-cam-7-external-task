package `in`.francl.cam.infrastructure.monitoring.instrumentation

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import org.apache.hc.core5.http.ClassicHttpRequest
import org.apache.hc.core5.http.ClassicHttpResponse
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.toJavaDuration

class ExternalTaskClientHttpClientMeasurement(
    private val meterRegistry: MeterRegistry,
) : Measurable {

    override fun measure(tags: HashMap<String, String>, duration: Duration) {
        Timer.builder("http_client_camunda_platform_rest_api")
            .tags(tags.map { (k, v) -> Tag.of(k, v) })
            .publishPercentiles(0.5, 0.90, 0.95, 0.99)
            .register(meterRegistry)
            .record(duration.toJavaDuration())
    }
}
