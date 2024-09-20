package `in`.francl.cam.infrastructure.config.module

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.koin
import org.koin.logger.slf4jLogger

class PluginModule : ModuleInitializer {
    override fun init(app: Application) {
        app.applyKoin()
        app.applyContentNegotiation()
        app.applyCompression()
        app.applyMetrics()
    }

    private fun Application.applyMetrics() {
        val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        install(MicrometerMetrics) {
            registry = meterRegistry
        }
        koin {
            modules(
                module {
                    single<MeterRegistry> { meterRegistry }
                }
            )
        }
    }
    private fun Application.applyKoin() {
        install(Koin) {
            slf4jLogger()
            modules(
                module {
                }
            )
        }
    }

    private fun Application.applyContentNegotiation() {
        install(ContentNegotiation) {
            json(Json {
                allowStructuredMapKeys = true
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private fun Application.applyCompression() {
        install(Compression) {
            gzip {
                priority = 1.0
            }
            deflate {
                priority = 10.0
                minimumSize(1024)
            }
        }
    }

}