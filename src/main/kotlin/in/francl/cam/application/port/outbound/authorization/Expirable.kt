package `in`.francl.cam.application.port.outbound.authorization

interface Expirable {
    fun isExpired(): Boolean
}