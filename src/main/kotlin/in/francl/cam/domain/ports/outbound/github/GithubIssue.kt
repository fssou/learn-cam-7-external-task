package `in`.francl.cam.domain.ports.outbound.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubIssue(
    val id: Long,
    val number: Long,
    val title: String,
    val user: GithubUser,
    val state: String,
    val locked: Boolean,
    val assignee: GithubUser?,
    val assignees: Set<GithubUser>,
    val comments: Long,
    @SerialName("created_at")
    val createdAt: String?,
    @SerialName("updated_at")
    val updatedAt: String?,
    @SerialName("closed_at")
    val closedAt: String?,
    @SerialName("author_association")
    val authorAssociation: String?,
    @SerialName("active_lock_reason")
    val activeLockReason: String?,
)
