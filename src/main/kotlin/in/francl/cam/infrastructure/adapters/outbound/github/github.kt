package `in`.francl.cam.infrastructure.adapters.outbound.github

import io.ktor.server.application.*
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import org.koin.ktor.plugin.koin

fun Application.adapterOutboundGithub() {
    koin {
        modules(
            module {
                single<`in`.francl.cam.domain.ports.outbound.github.GithubGateway> { GithubApi(get(StringQualifier("HttpClientWithAuth"))) }
            }
        )
    }
}
