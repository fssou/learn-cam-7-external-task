package `in`.francl.cam.infrastructure.client.http.plugin

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.util.*
import org.slf4j.LoggerFactory
import org.slf4j.MDC

class CorrelationId
private constructor(
    private val config: Configuration
) {

    private val logger = config.logger


    private fun install(scope: HttpClient) {
        scope.requestPipeline.intercept(HttpRequestPipeline.Before) {
            val correlationId = MDC.get("correlationId") ?: "unknown"
            logger.debug("CorrelationId: $correlationId")
            context.headers.append("x-correlationId", correlationId)
            proceed()
        }
    }

    class Configuration {
        var logger = LoggerFactory.getLogger(CorrelationId::class.java)!!
    }

    companion object Plugin : HttpClientPlugin<Configuration, CorrelationId> {
        override val key: AttributeKey<CorrelationId> = AttributeKey(CorrelationId::class.qualifiedName!!)

        override fun prepare(block: Configuration.() -> Unit): CorrelationId {
            val config = Configuration().apply(block)
            return CorrelationId(config)
        }

        override fun install(plugin: CorrelationId, scope: HttpClient) {
            plugin.install(scope)
        }
    }
}