package `in`.francl.cam.domain.error

interface InfrastructureError {
    val code: String
    val message: String
    val details: String
    val cause: Throwable?
}
