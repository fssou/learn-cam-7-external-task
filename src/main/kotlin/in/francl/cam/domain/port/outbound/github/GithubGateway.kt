package `in`.francl.cam.domain.port.outbound.github

interface GithubGateway {
    suspend fun getRepository(owner: String, repository: String): Result<GithubRepository>
    suspend fun getIssues(owner: String, repository: String): Result<Set<GithubIssue>>
}
