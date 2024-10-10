package `in`.francl.cam.infrastructure.client.http.plugin

import `in`.francl.cam.application.port.outbound.authorization.AuthorizationGateway
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.util.*
import org.slf4j.LoggerFactory

class Authorization
private constructor(
    private val config: Configuration
) {

    private val logger = config.logger
    private val gateway = config.gateway

    private fun install(scope: HttpClient) {
        scope.requestPipeline.intercept(HttpRequestPipeline.State) {
            val scopes = context.attributes[ATTRIBUTE_KEY_SCOPES]
            val tokenResult = gateway.retrieve(scopes)
            context.headers {
                tokenResult.onSuccess { token ->
                    append(HttpHeaders.Authorization, "${token.tokenType} ${token.accessToken}")
                }
            }
        }
    }

    class Configuration {
        var logger = LoggerFactory.getLogger(Authorization::class.java)!!
        lateinit var gateway: `in`.francl.cam.application.port.outbound.authorization.AuthorizationGateway
    }

    companion object Plugin : HttpClientPlugin<Configuration, Authorization> {
        val ATTRIBUTE_KEY_SCOPES = AttributeKey<Set<String>>("scopes")
        override val key: AttributeKey<Authorization> = AttributeKey(Authorization::class.qualifiedName!!)
        override fun prepare(block: Configuration.() -> Unit): Authorization {
            val config = Configuration().apply(block)
            return Authorization(config)
        }
        override fun install(plugin: Authorization, scope: HttpClient) {
            plugin.install(scope)
        }
    }
}