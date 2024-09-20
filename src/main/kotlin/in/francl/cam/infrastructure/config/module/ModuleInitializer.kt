package `in`.francl.cam.infrastructure.config.module

import io.ktor.server.application.*

internal interface ModuleInitializer {
    fun init(app: Application)
}