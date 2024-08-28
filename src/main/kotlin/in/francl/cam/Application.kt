package `in`.francl.cam

import `in`.francl.cam.infrastructure.infrastructure
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.config.yaml.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import java.io.File
import java.security.KeyStore


fun main() {
    val logger = LoggerFactory.getLogger(::main.javaClass)!!
    logger.info("Starting Application")
    val environment = applicationEngineEnvironment {
        val properties = YamlConfigLoader().load("application.yaml")!!
        val env = properties.tryGetString("env")!!
        config = properties
        log = logger
        developmentMode = env == "dev"
        val props = object {
            val host = config.property("server.host").getString()
            val port = config.property("server.port").getString().toInt()
            val keyStorePath = config.property("server.ssl.keyStore.path").getString()
            val keyStorePassword = config.property("server.ssl.keyStore.password").getString()
            val keyStoreAlias = config.property("server.ssl.keyStore.alias").getString()
            val privateKeyPassword = config.property("server.ssl.keyStore.privateKeyPassword").getString()
            val sslHost = config.property("server.ssl.host").getString()
            val sslPort = config.property("server.ssl.port").getString().toInt()
        }
        connector {
            host = props.host
            port = props.port
        }
        sslConnector(
            keyStore = KeyStore.getInstance(File(props.keyStorePath), props.keyStorePassword.toCharArray()),
            keyAlias = props.keyStoreAlias,
            keyStorePassword = { props.keyStorePassword.toCharArray() },
            privateKeyPassword = { props.privateKeyPassword.toCharArray() },
        ) {
            host = props.sslHost
            port = props.sslPort
        }
        module(Application::infrastructure)
    }
    embeddedServer(
        factory = Netty,
        environment = environment,
    ).start(
        wait = true
    )
}
