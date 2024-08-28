package `in`.francl.cam.infrastructure.config

import `in`.francl.cam.application.services.github.IssuesGithubReposTaskService
import `in`.francl.cam.domain.ports.inbound.task.TaskHandler
import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


fun Application.configureIoc() {
    install(Koin) {
        slf4jLogger()
        modules(
            module {
                single<Set<TaskHandler>> { // TODO: Verificar um lugar melhor para criar essa lista de handlers
                    setOf<TaskHandler>(
                        IssuesGithubReposTaskService(get())
                    )
                }
            }
        )
    }
}
