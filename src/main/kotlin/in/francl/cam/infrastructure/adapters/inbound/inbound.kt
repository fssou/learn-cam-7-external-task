package `in`.francl.cam.infrastructure.adapters.inbound

import `in`.francl.cam.infrastructure.adapters.inbound.camunda.adapterInboundCamunda
import `in`.francl.cam.infrastructure.adapters.inbound.web.adapterInboundWeb
import io.ktor.server.application.*

fun Application.adapterInbound() {
    adapterInboundWeb()
    adapterInboundCamunda()
}