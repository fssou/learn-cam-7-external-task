package `in`.francl.cam.infrastructure.adapter.outbound.http.authorization

import `in`.francl.cam.application.port.outbound.authorization.AuthorizationGateway
import `in`.francl.cam.application.port.outbound.authorization.OAuth2Credentials
import `in`.francl.cam.application.port.outbound.authorization.TokenExpirable
import `in`.francl.cam.application.port.outbound.authorization.Tokenable
import `in`.francl.cam.application.port.outbound.cache.Cacheable
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import net.logstash.logback.marker.Markers
import org.slf4j.LoggerFactory

class SecurityTokenServiceAdapter(
    private val httpClient: HttpClient,
    private val url: String,
    private val oAuth2Credentials: `in`.francl.cam.application.port.outbound.authorization.OAuth2Credentials,
    private val cache: `in`.francl.cam.application.port.outbound.cache.Cacheable<Set<String>, `in`.francl.cam.application.port.outbound.authorization.TokenExpirable>,
) : `in`.francl.cam.application.port.outbound.authorization.AuthorizationGateway {

    override suspend fun retrieve(
        scopes: Set<String>,
    ): Result<`in`.francl.cam.application.port.outbound.authorization.Tokenable> {
        val cachedToken = cache.get(scopes)
        if (cachedToken != null) {
            logger.info(
                Markers.append("token", hashMapOf("scopes" to scopes)),
                "Token retrieved from cache"
            )
            return Result.success(cachedToken)
        }
        val clientId = oAuth2Credentials.clientId
        val clientSecret = oAuth2Credentials.clientSecret
        val grantType = oAuth2Credentials.grantType
        val scopesInline = scopes.joinToString(" ")
        val response = httpClient.submitForm(
            url = "$url/auth/realms/master/protocol/openid-connect/token",
            formParameters = Parameters.build {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("grant_type", grantType)
                append("scope", scopesInline)
            }
        ) {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded)
        }
        if (response.status.value !in 200..299) {
            val exception = Exception(response.status.description, Exception(response.bodyAsText()))
            logger.error(
                Markers.appendEntries(
                    hashMapOf(
                        "credentials" to oAuth2Credentials.copy(clientSecret = "***"),
                    )
                ),
                "Error retrieving token: ${response.status.description}",
                exception,
            )
            return Result.failure(exception)
        }
        return runCatching {
            response.body<Token>()
        }
            .onSuccess {
                cache.put(scopes, TokenExpire.from(it))
                logger.info(
                    Markers.appendEntries(
                        hashMapOf(
                            "credentials" to oAuth2Credentials.copy(clientSecret = "***"),
                            "token" to it.copy(accessToken = "***", refreshToken = "***"),
                        )
                    ),
                    "Token retrieved successfully"
                )
            }
            .onFailure {
                logger.error(
                    Markers.appendEntries(
                        hashMapOf(
                            "credentials" to oAuth2Credentials.copy(clientSecret = "***"),
                        )
                    ),
                    "Error retrieving token",
                    it,
                )
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SecurityTokenServiceAdapter::class.java)!!
    }

}
