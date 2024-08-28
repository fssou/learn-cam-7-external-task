package `in`.francl.cam.infrastructure.adapters

import `in`.francl.cam.infrastructure.adapters.inbound.adapterInbound
import `in`.francl.cam.infrastructure.adapters.outbound.adapterOutbound
import io.ktor.server.application.*

fun Application.adapters() {
    adapterOutbound()
    adapterInbound()
}