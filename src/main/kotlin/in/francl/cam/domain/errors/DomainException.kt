package `in`.francl.cam.domain.errors

class DomainException(
    override val code: String,
    override val message: String,
    override val details: String?,
) : Exception(message), DomainError {

}
