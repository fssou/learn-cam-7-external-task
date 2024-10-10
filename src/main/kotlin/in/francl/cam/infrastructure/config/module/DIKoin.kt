package `in`.francl.cam.infrastructure.config.module

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

object DIKoin : (Application) -> Unit {
    override fun invoke(app: Application) {
        app.apply {
            install(Koin) {
                slf4jLogger()
                modules(
                    module {
                    }
                )
            }
        }
    }
}