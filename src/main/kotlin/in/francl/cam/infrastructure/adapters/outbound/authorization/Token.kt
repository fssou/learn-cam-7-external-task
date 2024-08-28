package `in`.francl.cam.infrastructure.adapters.outbound.authorization

import `in`.francl.cam.domain.ports.outbound.authorization.Tokenable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Token(
    @SerialName("access_token")
    override val accessToken: String,
    @SerialName("token_type")
    override val tokenType: String,
    @SerialName("expires_in")
    override val expiresIn: Int,
    @SerialName("refresh_token")
    override val refreshToken: String,
    @Serializable(ScopeSerializer::class)
    override val scope: Set<String>,
) : Tokenable
