package `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.ext.httpclient.interceptor

import `in`.francl.cam.infrastructure.monitoring.measurement.Measurable
import org.apache.hc.client5.http.classic.ExecChain
import org.apache.hc.client5.http.classic.ExecChainHandler
import org.apache.hc.core5.http.ClassicHttpRequest
import org.apache.hc.core5.http.ClassicHttpResponse
import kotlin.time.measureTimedValue

class Measurement(
    private val measurable: Measurable,
) : ExecChainHandler {
    override fun execute(
        request: ClassicHttpRequest,
        scope: ExecChain.Scope,
        chain: ExecChain
    ): ClassicHttpResponse {
        val timedResponse = measureTimedValue {
            chain.proceed(request, scope)
        }
        val response = timedResponse.value
        val duration = timedResponse.duration

        val tags = hashMapOf(
            "method" to request.method,
            "host" to (request.uri?.host ?: "unknown"),
            "path" to (request.uri?.path?.replace(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"), "{id}") ?: "unknown"),
            "status" to response.code.toString(),
        )
        measurable.measure(tags, duration)

        return response
    }
    companion object
}