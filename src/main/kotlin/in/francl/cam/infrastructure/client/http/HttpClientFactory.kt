package `in`.francl.cam.infrastructure.client.http

import `in`.francl.cam.domain.port.outbound.authorization.AuthorizationGateway
import `in`.francl.cam.infrastructure.client.http.plugin.Authorization
import `in`.francl.cam.infrastructure.client.http.plugin.CorrelationId
import `in`.francl.cam.infrastructure.client.http.plugin.Logging
import `in`.francl.cam.infrastructure.client.http.plugin.Measurement
import `in`.francl.cam.infrastructure.monitoring.measurement.Measurable
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class HttpClientFactory(
    private val measurable: Measurable,
    private val type: Type = Type.DEFAULT,
    private val authorizationGateway: AuthorizationGateway? = null,
) {

    fun create(): HttpClient {
        return when (type) {
            Type.DEFAULT -> createDefault()
        }
    }

    private fun createDefault(): HttpClient {
        return HttpClient(CIO) {
            engine {
                pipelining = true
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    }
                )
            }
            install(HttpRequestRetry) {
                maxRetries = 3
                constantDelay()
                retryOnServerErrors(3)
                retryIf(3) { _, response ->
                    response.status.value in setOf(401, 403)
                }
            }
            install(Logging)
            install(CorrelationId)
            install(Measurement) {
                measurable = this@HttpClientFactory.measurable
            }
            if (authorizationGateway != null) {
                install(Authorization) {
                    gateway = authorizationGateway
                }
            }
        }
    }

    enum class Type {
        DEFAULT,
    }

    companion object {
        private val logger = LoggerFactory.getLogger(HttpClientFactory::class.java)!!
    }
}
