package `in`.francl.cam.domain.ports.outbound.task

import `in`.francl.cam.domain.ports.inbound.task.Task

interface TaskLocker {

    suspend fun lock(task: Task, lockDuration: Long)

    suspend fun unlock(task: Task)

    suspend fun extendLock(task: Task, newDuration: Long)

}