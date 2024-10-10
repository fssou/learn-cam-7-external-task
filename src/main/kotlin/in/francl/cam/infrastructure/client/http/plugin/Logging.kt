package `in`.francl.cam.infrastructure.client.http.plugin

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.statement.*
import io.ktor.http.content.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import net.logstash.logback.marker.Markers
import org.slf4j.LoggerFactory

class Logging
private constructor(
    private val config: Configuration
) {

    private val logger = config.logger
    private val mapper = config.mapper


    private fun install(scope: HttpClient) {
        scope.responsePipeline.intercept(HttpResponsePipeline.Receive) {

            val request = context.request
            val response = context.response

            val requestBodyByteArray = runCatching {
                when (val content = request.content) {
                    is OutgoingContent.ByteArrayContent -> content.bytes()
                    is OutgoingContent.ReadChannelContent -> content.readFrom().toByteArray()
                    else -> null
                }
            }.getOrNull()
            val requestBody = runCatching {
                mapper.readValue(requestBodyByteArray, HashMap::class.java)
            }.getOrElse { requestBodyByteArray?.decodeToString() }

            val responseBodyByteArray = runCatching {
                when (val content = it.response) {
                    is ByteReadChannel -> content.readRemaining().readBytes()
                    else -> null
                }
            }.getOrNull()
            val responseBody = runCatching {
                mapper.readValue(responseBodyByteArray, HashMap::class.java)
            }.getOrElse { responseBodyByteArray?.decodeToString() }

            val http = hashMapOf(
                "request" to hashMapOf(
                    "method" to request.method.value,
                    "host" to request.url.host,
                    "path" to request.url.encodedPath,
                    "query" to request.url.encodedQuery,
                    "headers" to request.headers.toMap(),
                    "body" to requestBody
                ),
                "response" to hashMapOf(
                    "status" to response.status.value,
                    "headers" to response.headers.toMap(),
                    "body" to responseBody
                )
            )
            logger.info(
                Markers.append("http", http),
                "HTTP Client"
            )
            proceedWith(HttpResponseContainer(it.expectedType, ByteReadChannel(responseBodyByteArray ?: byteArrayOf())))
        }
    }

    class Configuration {
        var logger = LoggerFactory.getLogger(Logging::class.java)!!
        var mapper = jacksonObjectMapper()
    }

    companion object Plugin : HttpClientPlugin<Configuration, Logging> {
        override val key: AttributeKey<Logging> = AttributeKey(Logging::class.qualifiedName!!)

        override fun prepare(block: Configuration.() -> Unit): Logging {
            val config = Configuration().apply(block)
            return Logging(config)
        }

        override fun install(plugin: Logging, scope: HttpClient) {
            plugin.install(scope)
        }
    }
}