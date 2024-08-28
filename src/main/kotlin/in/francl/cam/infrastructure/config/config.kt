package `in`.francl.cam.infrastructure.config

import io.ktor.server.application.*

fun Application.config() {
    configureIoc()
    configureHTTP()
    configureSerialization()
}
