package `in`.francl.cam.application.annotation

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class TaskService(
    val name: String,
    val lockDuration: Long,
) {

}
