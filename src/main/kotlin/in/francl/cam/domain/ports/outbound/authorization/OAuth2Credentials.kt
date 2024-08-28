package `in`.francl.cam.domain.ports.outbound.authorization

data class OAuth2Credentials(
    val clientId: String,
    val clientSecret: String,
    val grantType: String,
)
