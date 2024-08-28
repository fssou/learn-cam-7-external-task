package `in`.francl.cam.infrastructure.monitoring.instrumentation

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import okhttp3.Response
import java.util.concurrent.TimeUnit

class HttpClientInstrumentation(
    private val meterRegistry: MeterRegistry,
) {

    fun measureResponseTime(
        block: () -> Response
    ) : Response {
        val initialTime = System.currentTimeMillis()
        val response = block()
        val time = System.currentTimeMillis() - initialTime
        Timer.builder("http_client")
            .tag("address", response.request.url.host)
            .tag("method", response.request.method)
            .tag("route", response.request.url.encodedPath.replace(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}|[0-9]*"), "{id}"))
            .tag("status", response.code.toString())
            .publishPercentiles(0.5, 0.90, 0.95, 0.99)
            .register(meterRegistry)
            .record(time, TimeUnit.MILLISECONDS)
        return response
    }
}
