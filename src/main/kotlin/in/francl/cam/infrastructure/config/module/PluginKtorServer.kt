package `in`.francl.cam.infrastructure.config.module

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.compression.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

object PluginKtorServer : (Application) -> Unit {
    override fun invoke(app: Application) {
        app.apply {
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