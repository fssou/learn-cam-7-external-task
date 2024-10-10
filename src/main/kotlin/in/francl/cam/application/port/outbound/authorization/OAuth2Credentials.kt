package `in`.francl.cam.application.port.outbound.authorization

data class OAuth2Credentials(
    val clientId: String,
    val clientSecret: String,
    val grantType: String,
)
