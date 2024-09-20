package `in`.francl.cam.application.error

class FailureServiceError(
    override val code: String,
    override val message: String,
    override val details: String,
    override val cause: Throwable,
    val variables: Map<String, Any?>,
    /**
     * Numero de tentativas. 0 para não tentar.
     */
    val retries: Int,
    val retryTimeout: Long,
) : Exception(message, cause), ServiceError {

}