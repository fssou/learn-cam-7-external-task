package `in`.francl.cam.infrastructure.adapters.outbound

import `in`.francl.cam.infrastructure.adapters.outbound.authorization.adapterOutboundAuthorization
import `in`.francl.cam.infrastructure.adapters.outbound.camunda.adapterOutboundCamunda
import `in`.francl.cam.infrastructure.adapters.outbound.github.adapterOutboundGithub
import `in`.francl.cam.infrastructure.adapters.outbound.hc.adapterOutboundHc
import io.ktor.server.application.*

fun Application.adapterOutbound() {
    adapterOutboundHc()
    adapterOutboundAuthorization()
    adapterOutboundGithub()
    adapterOutboundCamunda()
}
