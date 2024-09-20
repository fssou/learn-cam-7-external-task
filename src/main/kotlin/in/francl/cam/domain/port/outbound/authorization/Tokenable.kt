package `in`.francl.cam.domain.port.outbound.authorization

interface Tokenable {
    val accessToken: String
    val tokenType: String
    val expiresIn: Int
    val refreshToken: String
    val scope: Set<String>
}