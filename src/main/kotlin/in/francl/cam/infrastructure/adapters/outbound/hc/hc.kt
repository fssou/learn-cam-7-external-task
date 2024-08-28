package `in`.francl.cam.infrastructure.adapters.outbound.hc

import `in`.francl.cam.infrastructure.adapters.outbound.hc.interceptors.LoggingInterceptor
import `in`.francl.cam.infrastructure.adapters.outbound.hc.interceptors.InstrumentationInterceptor
import `in`.francl.cam.infrastructure.monitoring.instrumentation.HttpClientInstrumentation
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.koin

fun Application.adapterOutboundHc() {
    koin {
        val registry = get<PrometheusMeterRegistry>()
        val httpClientInstrumentation = HttpClientInstrumentation(registry)

        val httpClient = HttpClient(OkHttp) {
            engine {
                pipelining = true
                addInterceptor(LoggingInterceptor())
                addInterceptor(InstrumentationInterceptor(httpClientInstrumentation))
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
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
            install("CorrelationId") {
                requestPipeline.intercept(HttpRequestPipeline.Before) {
                    val correlationId = coroutineContext[MDCContext.Key]?.contextMap?.get("correlationId") ?: "unknown"
                    context.headers.append("x-correlationId", correlationId)
                    proceed()
                }
            }
        }

        modules(
            module {
                single<HttpClient> { httpClient }
            }
        )
    }
}