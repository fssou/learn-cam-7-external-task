package `in`.francl.cam.application.services.github

import arrow.core.Either
import `in`.francl.cam.application.annotation.TaskService
import `in`.francl.cam.application.dto.TaskResult
import `in`.francl.cam.application.errors.BusinessServiceError
import `in`.francl.cam.application.errors.FailureServiceError
import `in`.francl.cam.application.errors.ServiceError
import `in`.francl.cam.application.services.BaseTaskService
import `in`.francl.cam.domain.ports.inbound.task.Task
import `in`.francl.cam.domain.ports.outbound.github.GithubGateway
import `in`.francl.cam.domain.ports.outbound.task.TaskLocker

/**
 * Task service to get information about a GitHub repository
 * @param githubGateway Gateway to communicate with GitHub API
 */
@TaskService(
    name = "IssuesGithubReposTaskService",
    lockDuration = 20000,
)
class IssuesGithubReposTaskService(
    private val githubGateway: GithubGateway,
) : BaseTaskService() {

    override suspend fun perform(task: Task, taskLocker: TaskLocker): Either<ServiceError, TaskResult> {
        val owner: String = task.variables["owner"]?.toString() ?: return Either.Left(
            BusinessServiceError(
                "GH-4-00",
                "owner não encontrado",
                "owner não encontrado",
                Exception("owner não encontrado"),
                mapOf()
            )
        )
        val repository: String = task.variables["repository"]?.toString() ?: return Either.Left(
            BusinessServiceError(
                "GH-4-07",
                "repository não encontrado",
                "repository não encontrado",
                Exception("repository não encontrado"),
                mapOf()
            )
        )
        val repositoryResult = githubGateway.getRepository(owner, repository)
        repositoryResult.onSuccess { repos ->
            val issuesResult = githubGateway.getIssues(owner, repository)
            issuesResult.onSuccess { issues ->
                return Either.Right(
                    TaskResult(
                        hashMapOf(
                            "repository" to repos.name,
                            "owner" to repos.owner.login,
                            "description" to repos.description,
                            "stargazersCount" to repos.stargazersCount,
                            "watchersCount" to repos.watchersCount,
                            "forksCount" to repos.forksCount,
                            "openIssuesCount" to repos.openIssuesCount,
                            "subscribersCount" to repos.subscribersCount,
                            "issues" to issues.map { issue ->
                                hashMapOf(
                                    "number" to issue.number,
                                    "title" to issue.title,
                                    "state" to issue.state,
                                    "createdAt" to issue.createdAt,
                                    "updatedAt" to issue.updatedAt,
                                    "closedAt" to issue.closedAt,
                                    "comments" to issue.comments,
                                )
                            },
                        )
                    )
                )
            }.onFailure {
                return Either.Left(
                    FailureServiceError(
                        "GH-5-04",
                        "Erro ao buscar issues",
                        it.stackTraceToString(),
                        it,
                        mapOf(),
                        0,
                        0,
                    )
                )
            }
        }.onFailure {
            return Either.Left(
                FailureServiceError(
                    "GH-5-02",
                    "Erro ao buscar repositorios",
                    it.message ?: "Erro ao buscar repositorios",
                    it,
                    mapOf(),
                    0,
                    0,
                )
            )
        }
        return Either.Left(
            FailureServiceError(
                "XXX000",
                "Erro ao executar passo específico",
                "Erro ao executar passo específico",
                Exception("Erro ao executar passo específico"),
                mapOf(),
                0,
                0,
            )
        )
    }

}
