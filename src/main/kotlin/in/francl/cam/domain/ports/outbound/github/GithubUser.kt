package `in`.francl.cam.domain.ports.outbound.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubUser(
    val login: String,
    val id: Long,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("gravatar_id")
    val gravatarId: String?,
    val type: String,
    @SerialName("site_admin")
    val siteAdmin: Boolean
)