package `in`.francl.cam.domain.errors

interface InfrastructureError {
    val code: String
    val message: String
    val details: String
    val cause: Throwable?
}
