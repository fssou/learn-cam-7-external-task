package `in`.francl.cam.infrastructure.cache

import `in`.francl.cam.domain.ports.outbound.authorization.Expirable

interface Cache<K, V : Expirable> {
    fun get(key: K): V?
    fun put(key: K, value: V)
}