package `in`.francl.cam.infrastructure

import `in`.francl.cam.infrastructure.adapters.adapters
import `in`.francl.cam.infrastructure.config.config
import `in`.francl.cam.infrastructure.monitoring.monitoring
import io.ktor.server.application.*

fun Application.infrastructure() {
    config()
    monitoring()
    adapters()
}
