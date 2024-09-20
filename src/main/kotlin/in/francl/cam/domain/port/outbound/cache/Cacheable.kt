package `in`.francl.cam.domain.port.outbound.cache

import `in`.francl.cam.domain.port.outbound.authorization.Expirable

interface Cacheable<K, V : Expirable> {
    fun get(key: K): V?
    fun put(key: K, value: V)
}