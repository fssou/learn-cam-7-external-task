package `in`.francl.cam.domain.errors

interface DomainError {
    val code: String
    val message: String
    val details: String?
}
