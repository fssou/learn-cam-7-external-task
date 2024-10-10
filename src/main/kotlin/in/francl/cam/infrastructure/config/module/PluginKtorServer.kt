package `in`.francl.cam.infrastructure.config.module

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import io.micrometer.core.instrument.MeterRegistry
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.get

object PluginKtorServer : (Application) -> Unit {
    override fun invoke(app: Application) {
        app.apply {
            val meterRegistry = get<MeterRegistry>()

            install(MicrometerMetrics) {
                registry = meterRegistry
            }
            install(ContentNegotiation) {
                json(Json {
                    allowStructuredMapKeys = true
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
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
}