package `in`.francl.cam.infrastructure.config.module

import `in`.francl.cam.application.handler.DefaultTaskHandler
import `in`.francl.cam.application.handler.TaskHandlerRegistry
import `in`.francl.cam.application.service.httpcodes.HttpCodesPerformable
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.koin
import org.slf4j.LoggerFactory

object DITaskHandler : (Application) -> Unit {
    private val logger = LoggerFactory.getLogger(DITaskHandler::class.java)!!
    override fun invoke(app: Application) {
        app.apply {
            val taskHandlerRegistry = TaskHandlerRegistry()

            taskHandlerRegistry.register(DefaultTaskHandler(HttpCodesPerformable())) {
                topicName = "topic"
                lockDuration = 10000
            }

            koin {
                modules(
                    module {
                        single { taskHandlerRegistry }
                    }
                )
            }
        }
    }
}