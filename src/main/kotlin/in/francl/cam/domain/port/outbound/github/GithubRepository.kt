package `in`.francl.cam.domain.port.outbound.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GithubRepository(
    val id: Long,
    @SerialName("node_id")
    val nodeId: String,
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    @SerialName("is_private")
    val isPrivate: Boolean,
    val owner: GithubUser,
    val description: String,
    val fork: Boolean,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("pushed_at")
    val pushedAt: String,
    val homepage: String?,
    val size: Int,
    @SerialName("stargazers_count")
    val stargazersCount: Int,
    @SerialName("watchers_count")
    val watchersCount: Int,
    val language: String,
    @SerialName("has_issues")
    val hasIssues: Boolean,
    @SerialName("has_projects")
    val hasProjects: Boolean,
    @SerialName("has_downloads")
    val hasDownloads: Boolean,
    @SerialName("has_wiki")
    val hasWiki: Boolean,
    @SerialName("has_pages")
    val hasPages: Boolean,
    @SerialName("has_discussions")
    val hasDiscussions: Boolean,
    @SerialName("forks_count")
    val forksCount: Int,
    val archived: Boolean,
    val disabled: Boolean,
    @SerialName("open_issues_count")
    val openIssuesCount: Int,
    @SerialName("allow_forking")
    val allowForking: Boolean,
    @SerialName("is_template")
    val isTemplate: Boolean,
    @SerialName("web_commit_signoff_required")
    val webCommitSignoffRequired: Boolean,
    val topics: List<String>,
    val visibility: String,
    val forks: Int,
    @SerialName("open_issues")
    val openIssues: Int,
    @SerialName("default_branch")
    val defaultBranch: String,
    @SerialName("temp_clone_token")
    val tempCloneToken: String?,
    @SerialName("network_count")
    val networkCount: Int,
    @SerialName("subscribers_count")
    val subscribersCount: Int
)
