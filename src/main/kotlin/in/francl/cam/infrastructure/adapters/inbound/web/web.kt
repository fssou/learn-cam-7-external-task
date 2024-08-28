package `in`.francl.cam.infrastructure.adapters.inbound.web

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.koin.ktor.ext.get

fun Application.adapterInboundWeb() {
    routing {
        get("/monitoring/metrics") {
            call.respond(this@adapterInboundWeb.get<PrometheusMeterRegistry>().scrape("application/openmetrics-text"))
        }
        get("/monitoring/health") {
            call.respondText(text = "UP", status = HttpStatusCode.OK)
        }
    }
}
