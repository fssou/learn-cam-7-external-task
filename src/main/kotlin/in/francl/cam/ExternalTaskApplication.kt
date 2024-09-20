package `in`.francl.cam

import `in`.francl.cam.infrastructure.config.app.EnvironmentFactory
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.slf4j.LoggerFactory

class ExternalTaskApplication {
    companion object {
        private val logger = LoggerFactory.getLogger(ExternalTaskApplication::class.java)!!

        @JvmStatic
        fun main(args: Array<String>) {
            logger.info("Starting Application")
            val environment = EnvironmentFactory().create()

            val engine = embeddedServer(
                factory = CIO,
                environment = environment,
            )

            engine.addShutdownHook {
                logger.info("Stopping Application")
            }

            engine.start(
                wait = true,
            )
        }
    }
}
