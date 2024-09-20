package `in`.francl.cam.infrastructure.config.app

import `in`.francl.cam.infrastructure.config.module.AdapterInboundModule
import `in`.francl.cam.infrastructure.config.module.AdapterOutboundModule
import `in`.francl.cam.infrastructure.config.module.PluginModule
import io.ktor.server.engine.*
import org.slf4j.LoggerFactory

internal class EnvironmentFactory(
    private val type: Type = Type.DEFAULT,
) {

    fun create(): ApplicationEngineEnvironment {
        return when (type) {
            Type.DEFAULT -> createDefault()
        }
    }

    private fun createDefault(): ApplicationEngineEnvironment {
        return applicationEngineEnvironment {
            config = Configuration().load()
            log = logger
            developmentMode = config.property("env").getString() == "local"
            connector {
                host = config.property("server.host").getString()
                port = config.property("server.port").getString().toInt()
            }

            module(PluginModule()::init)
            module(AdapterOutboundModule()::init)
            module(AdapterInboundModule()::init)
        }
    }
    enum class Type {
        DEFAULT,
    }
    companion object {
        private val logger = LoggerFactory.getLogger(EnvironmentFactory::class.java)!!
    }
}
