package `in`.francl.cam.infrastructure.adapters.outbound.authorization

import `in`.francl.cam.domain.ports.outbound.authorization.AuthorizationGateway
import `in`.francl.cam.domain.ports.outbound.authorization.TokenExpirable
import `in`.francl.cam.infrastructure.cache.SimpleCache
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.util.*
import org.koin.core.qualifier.StringQualifier
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.koin

fun Application.adapterOutboundAuthorization() {
    val url = environment.config.property("auth.url").getString()
    val oAuth2Credentials = `in`.francl.cam.domain.ports.outbound.authorization.OAuth2Credentials(
        clientId = environment.config.property("auth.clientId").getString(),
        clientSecret = environment.config.property("auth.clientSecret").getString(),
        grantType = environment.config.property("auth.grantType").getString(),
    )
    val httpClient = get<HttpClient>()
    val cache = SimpleCache<Set<String>, TokenExpirable>()
    val authorizationGateway = SecurityTokenServiceAdapter(httpClient, url, oAuth2Credentials, cache)
    val httpClientWithAuth = httpClient.config {
        install("Authorization") {
            requestPipeline.intercept(HttpRequestPipeline.State) {
                context.attributes.getOrNull(AttributeKey<Set<String>>("auth_scopes"))?.let { scopes ->
                    val tokenResult = authorizationGateway.retrieve(scopes)
                    context.headers {
                        tokenResult.onSuccess { token ->
                            append("Authorization", "Bearer ${token.accessToken}")
                        }
                    }
                }
            }
        }
    }
    koin {
        modules(
            module {
                single<AuthorizationGateway> { authorizationGateway }
                single(StringQualifier("HttpClientWithAuth")) { httpClientWithAuth }
            }
        )
    }
}
