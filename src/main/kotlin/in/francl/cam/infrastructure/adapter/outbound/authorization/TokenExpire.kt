package `in`.francl.cam.infrastructure.adapter.outbound.authorization

import java.time.Instant

data class TokenExpire(
    override val accessToken: String,
    override val tokenType: String,
    override val expiresIn: Int,
    override val refreshToken: String,
    override val scope: Set<String>,
) : `in`.francl.cam.domain.port.outbound.authorization.TokenExpirable {
    private val expiredAt: Instant = Instant.now().plusSeconds(expiresIn.toLong())
    override fun isExpired(): Boolean = Instant.now().isAfter(expiredAt.minusSeconds(10))

    companion object {
        fun from(token: Token): TokenExpire = TokenExpire(
            accessToken = token.accessToken,
            tokenType = token.tokenType,
            expiresIn = token.expiresIn,
            refreshToken = token.refreshToken,
            scope = token.scope
        )
    }
}
