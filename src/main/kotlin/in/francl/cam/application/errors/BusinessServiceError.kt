package `in`.francl.cam.application.errors

class BusinessServiceError(
    override val code: String,
    override val message: String,
    override val details: String,
    override val cause: Throwable,
    val variables: Map<String, Any?>,
) : Exception(message, cause), ServiceError {

}