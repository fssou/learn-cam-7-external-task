package `in`.francl.cam.infrastructure.monitoring.instrumentation

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.apache.hc.core5.http.ClassicHttpRequest
import org.apache.hc.core5.http.ClassicHttpResponse
import java.util.concurrent.TimeUnit

class ExternalTaskClientHttpClientInstrumentation(
    private val meterRegistry: MeterRegistry,
) {

    fun measureResponseTime(
        request: ClassicHttpRequest,
        block: () -> ClassicHttpResponse
    ): ClassicHttpResponse {
        val initialTime = System.currentTimeMillis()
        val response = block()
        val time = System.currentTimeMillis() - initialTime
        Timer.builder("http_client_camunda_platform_rest_api")
            .tag("method", request.method)
            .tag("address", request.uri?.host ?: "unknown")
            .tag("route", request.uri?.path?.replace(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"), "{id}") ?: "unknown")
            .tag("status", response.code.toString())
            .publishPercentiles(0.5, 0.90, 0.95, 0.99)
            .register(meterRegistry)
            .record(time, TimeUnit.MILLISECONDS)
        return response
    }
}
