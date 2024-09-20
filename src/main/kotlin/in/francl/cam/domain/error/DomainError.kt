package `in`.francl.cam.domain.error

interface DomainError {
    val code: String
    val message: String
    val details: String?
}
