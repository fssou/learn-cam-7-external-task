package `in`.francl.cam.domain.errors

class InfrastructureException(
    override val code: String,
    override val message: String,
    override val details: String,
    override val cause: Throwable?,
) : Exception(message, cause), InfrastructureError {

}