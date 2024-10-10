package `in`.francl.cam.application.port.outbound.cache

import `in`.francl.cam.application.port.outbound.authorization.Expirable

interface Cacheable<K, V : `in`.francl.cam.application.port.outbound.authorization.Expirable> {
    fun get(key: K): V?
    fun put(key: K, value: V)
}