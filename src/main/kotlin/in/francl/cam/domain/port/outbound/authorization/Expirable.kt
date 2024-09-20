package `in`.francl.cam.domain.port.outbound.authorization

interface Expirable {
    fun isExpired(): Boolean
}