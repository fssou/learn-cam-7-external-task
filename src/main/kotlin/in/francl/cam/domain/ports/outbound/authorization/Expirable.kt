package `in`.francl.cam.domain.ports.outbound.authorization

interface Expirable {
    fun isExpired(): Boolean
}