package `in`.francl.cam.infrastructure.config.app

import io.ktor.server.config.*
import io.ktor.server.config.yaml.*

internal class Configuration {
    private val filename = "application.yaml"
    fun load() : ApplicationConfig {
        return YamlConfigLoader().load(filename)!!
    }

}