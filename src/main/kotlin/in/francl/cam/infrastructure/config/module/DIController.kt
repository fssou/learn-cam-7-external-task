package `in`.francl.cam.infrastructure.config.module

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.koin.ktor.ext.get

object DIController : (Application) -> Unit {
    override fun invoke(app: Application) {
        app.apply {
            routing {
                val meterRegistry = get<MeterRegistry>()
                get("/monitoring/metrics") {
                    when (meterRegistry) {
                        is PrometheusMeterRegistry -> {
                            call.respond(meterRegistry.scrape("application/openmetrics-text"))
                        }
                    }
                }
                get("/monitoring/health") {
                    call.respondText(text = "UP", status = HttpStatusCode.OK)
                }
            }
        }
    }
}