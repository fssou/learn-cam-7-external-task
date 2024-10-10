package `in`.francl.cam.application.port.outbound.task

import `in`.francl.cam.domain.model.Task

interface TaskLocker {

    suspend fun lock(task: Task, lockDuration: Long) : Result<Unit>

    suspend fun unlock(task: Task) : Result<Unit>

    suspend fun extendLock(task: Task, newDuration: Long) : Result<Unit>

}