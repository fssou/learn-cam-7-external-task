package `in`.francl.cam.infrastructure.config.module

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.logger.slf4jLogger

object Koin : (Application) -> Unit {
    override fun invoke(app: Application) {
        app.apply {
            install(org.koin.ktor.plugin.Koin) {
                slf4jLogger()
                modules(
                    module {
                    }
                )
            }
        }
    }
}