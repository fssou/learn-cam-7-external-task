package `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.ext.httpclient.interceptor

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.logstash.logback.marker.Markers
import org.apache.hc.client5.http.classic.ExecChain
import org.apache.hc.client5.http.classic.ExecChainHandler
import org.apache.hc.core5.http.ClassicHttpRequest
import org.apache.hc.core5.http.ClassicHttpResponse
import org.apache.hc.core5.http.ContentType
import org.apache.hc.core5.http.io.entity.ByteArrayEntity
import org.apache.hc.core5.http.io.entity.EntityUtils
import org.slf4j.LoggerFactory

class Logging : ExecChainHandler {
    private val mapper = jacksonObjectMapper()
    override fun execute(
        request: ClassicHttpRequest,
        scope: ExecChain.Scope,
        chain: ExecChain
    ): ClassicHttpResponse {
        val response = chain.proceed(request, scope)

        val requestBody = runCatching { EntityUtils.toByteArray(request.entity) }
            .mapCatching { byteArray ->
                runCatching {
                    mapper.readValue(byteArray, HashMap::class.java)
                }
                    .onFailure { return@mapCatching byteArray.toString() }
                    .getOrNull()
            }
            .getOrNull()

        val responseBody = runCatching { EntityUtils.toByteArray(response.entity) }
            .mapCatching { byteArray ->
                runCatching {
                    response.entity = ByteArrayEntity(byteArray, ContentType.create(response.entity.contentType))
                    mapper.readValue(byteArray, HashMap::class.java)
                }
                    .onFailure { return@mapCatching byteArray.toString() }
                    .getOrNull()
            }
            .getOrNull()

        val http: HashMap<String, Any?> = hashMapOf(
            "request" to hashMapOf(
                "url" to request.uri.toString(),
                "method" to request.method,
                "headers" to request.headers.associate { h -> h.name to h.value },
                "body" to requestBody,
            ),
            "response" to hashMapOf(
                "status" to response.code,
                "headers" to response.headers.associate { h -> h.name to h.value },
                "body" to responseBody,
            ),
        )

        val httpMarker = Markers.append("http", http)!!

        logger.info(httpMarker, "Camunda API call")
        return response
    }

    companion object {
        private val logger = LoggerFactory.getLogger(Logging::class.java)!!
    }
}