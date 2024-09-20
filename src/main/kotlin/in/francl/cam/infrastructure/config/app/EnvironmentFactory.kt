package `in`.francl.cam.infrastructure.config.app

import `in`.francl.cam.infrastructure.config.module.*
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

            module(Koin)
            module(PluginKtorServer)
            module(Metrics)
            module(DIAuthorization)
            module(DICamunda)
            module(DIController)
        }
    }
    enum class Type {
        DEFAULT,
    }
    companion object {
        private val logger = LoggerFactory.getLogger(EnvironmentFactory::class.java)!!
    }
}
