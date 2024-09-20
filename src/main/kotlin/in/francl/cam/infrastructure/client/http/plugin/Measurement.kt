package `in`.francl.cam.infrastructure.client.http.plugin

import `in`.francl.cam.infrastructure.monitoring.instrumentation.Measurable
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Measurement
private constructor(
    private val config: Configuration
) {

    private val logger = config.logger
    private val measurable = config.measurable

    private val onCallTimeRequestKey = AttributeKey<Long>("onCallTimeRequestKey")


    private fun install(scope: HttpClient) {
        scope.requestPipeline.intercept(HttpRequestPipeline.Render) {
            val onCallTime = System.currentTimeMillis()
            context.attributes.put(onCallTimeRequestKey, onCallTime)
            proceed()
        }
        scope.responsePipeline.intercept(HttpResponsePipeline.Receive) {
            val onCallTime = context.attributes[onCallTimeRequestKey]
            val responseTime = (System.currentTimeMillis() - onCallTime).toDuration(DurationUnit.MILLISECONDS)
            val request = context.request
            val response = context.response
            val tags = hashMapOf(
                "address" to request.url.host,
                "path" to request.url.encodedPath.replace(Regex("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}|[0-9]+"), "{id}"),
                "method" to request.method.value,
                "status" to response.status.value.toString(),
            )
            measurable.measure(tags, responseTime)
            proceed()
        }
    }

    class Configuration {
        var logger = LoggerFactory.getLogger(Measurement::class.java)!!
        lateinit var measurable: Measurable
    }

    companion object Plugin : HttpClientPlugin<Configuration, Measurement> {
        override val key: AttributeKey<Measurement> = AttributeKey(Measurement::class.qualifiedName!!)

        override fun prepare(block: Configuration.() -> Unit): Measurement {
            val config = Configuration().apply(block)
            return Measurement(config)
        }

        override fun install(plugin: Measurement, scope: HttpClient) {
            plugin.install(scope)
        }
    }
}