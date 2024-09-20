package `in`.francl.cam.domain.port.outbound.task

import `in`.francl.cam.domain.model.Task


interface TaskResultSender {

    suspend fun success(task: Task, variables: Map<String, Any?>, localVariables: Map<String, Any?>) : Result<Unit>

    suspend fun failure(task: Task, errorMessage: String, errorDetails: String, retries: Int, retryTimeout: Long, variables: Map<String, Any?>, localVariables: Map<String, Any?>) : Result<Unit>

    suspend fun error(task: Task, code: String, message: String, variables: Map<String, Any?>) : Result<Unit>

}
