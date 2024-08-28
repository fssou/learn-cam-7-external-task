package `in`.francl.cam.infrastructure.adapters.outbound.camunda

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.koin

fun Application.adapterOutboundCamunda() {
    koin {
        modules(
            module {
                single<`in`.francl.cam.domain.ports.outbound.task.TaskManager> { ExternalTaskServiceAdapter(it.get(), it.get()) }
            }
        )
    }
}
