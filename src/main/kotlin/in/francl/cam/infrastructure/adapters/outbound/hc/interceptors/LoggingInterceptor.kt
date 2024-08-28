package `in`.francl.cam.infrastructure.adapters.outbound.hc.interceptors

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.client.request.*
import io.ktor.util.*
import net.logstash.logback.marker.Markers
import okhttp3.Interceptor
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.apache.hc.client5.http.config.RequestConfig
import org.slf4j.LoggerFactory

class LoggingInterceptor : Interceptor {

    companion object {
        private val logger = LoggerFactory.getLogger(LoggingInterceptor::class.java)!!
        private val objectMapper = jacksonObjectMapper()
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val attributeKey = AttributeKey<String>("route_template")
        val routeTemplate = request.tag(Attributes::class.java)?.getOrNull(attributeKey)

        val newRequest = request.newBuilder()
            .build()

        val requestBody = newRequest.body?.let { body ->
            val buffer = okio.Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        }

        val response = chain.proceed(
            newRequest.newBuilder()
                .method(
                    newRequest.method,
                    requestBody?.toRequestBody(newRequest.body?.contentType())
                )
                .build()
        )

        val responseBody = response.body?.string()

        val http = hashMapOf(
            "request" to hashMapOf(
                "method" to request.method,
                "url" to request.url.toString(),
                "headers" to request.headers.toMultimap(),
                "body" to requestBody?.let { runCatching {
                    objectMapper.readValue(it, HashMap::class.java)
                }.getOrElse {
                    requestBody
                }},
            ),
            "response" to hashMapOf(
                "headers" to response.headers.toMultimap(),
                "statusCode" to response.code.toString(),
                "body" to responseBody?.let { runCatching {
                    objectMapper.readValue(it, HashMap::class.java)
                }.getOrElse {
                    responseBody
                }},
            )
        )

        if (response.code !in 200..299) {
            logger.error(Markers.append("http", http), "API")
        } else {
            logger.info(Markers.append("http", http), "API")
        }

        // Reconstrói a resposta para que o corpo possa ser lido novamente
        return response.newBuilder()
            .body(responseBody?.toResponseBody(response.body?.contentType()))
            .build()
    }
}