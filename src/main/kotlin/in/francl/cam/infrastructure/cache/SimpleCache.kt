package `in`.francl.cam.infrastructure.cache

import `in`.francl.cam.domain.port.outbound.authorization.Expirable
import `in`.francl.cam.domain.port.outbound.cache.Cacheable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class SimpleCache<K, V : Expirable> : Cacheable<K, V> {
    private val storage = ConcurrentHashMap<K, V>()

    private suspend fun cleanup() = coroutineScope {
        storage.entries.removeIf { it.value.isExpired() }
    }

    override fun get(key: K): V? {
        val value = storage[key] ?: return null
        if (value.isExpired()) {
            storage.remove(key)
            return null
        }
        return value
    }

    override fun put(key: K, value: V) {
        storage[key] = value
        CoroutineScope(Dispatchers.Default).launch {
            cleanup()
        }
    }

}
