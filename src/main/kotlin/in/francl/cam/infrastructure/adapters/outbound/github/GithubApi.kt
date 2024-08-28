package `in`.francl.cam.infrastructure.adapters.outbound.github

import `in`.francl.cam.domain.errors.InfrastructureException
import `in`.francl.cam.domain.ports.outbound.github.GithubGateway
import `in`.francl.cam.domain.ports.outbound.github.GithubIssue
import `in`.francl.cam.domain.ports.outbound.github.GithubRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import org.slf4j.MDC

class GithubApi(
    private val httpClient: HttpClient,
) : GithubGateway {

    override suspend fun getRepository(
        owner: String,
        repository: String
    ): Result<GithubRepository> {
        val response = httpClient.get("https://api.github.com/repos/$owner/$repository") {
            setAttributes {
                put(AttributeKey("auth_scopes"), setOf("github-repos.read"))
            }
        }
        if (response.status.value !in 200..299) {
            return Result.failure(
                InfrastructureException(
                    response.status.value.toString(),
                    "Erro ao buscar repositório",
                    response.bodyAsText(),
                    null,
                )
            )
        }
        return runCatching { response.body<GithubRepository>() }
    }

    override suspend fun getIssues(
        owner: String,
        repository: String
    ): Result<Set<GithubIssue>> {
        val response = httpClient.get("https://api.github.com/repos/$owner/$repository/issues")
        if (response.status.value !in 200..299) {
            return Result.failure(
                InfrastructureException(
                    response.status.value.toString(),
                    "Erro ao buscar issues",
                    response.bodyAsText(),
                    null,
                )
            )
        }
        return runCatching { response.body<Set<GithubIssue>>() }
    }
}