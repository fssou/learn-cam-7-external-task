package `in`.francl.cam.application.error

interface ServiceError {
    val code: String
    val message: String
    val details: String
    val cause: Throwable
}

