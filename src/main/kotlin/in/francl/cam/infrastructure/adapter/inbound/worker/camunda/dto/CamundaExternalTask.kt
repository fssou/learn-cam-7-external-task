package `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.dto

import `in`.francl.cam.domain.model.Task
import org.camunda.bpm.client.task.ExternalTask
import java.time.Instant

class CamundaExternalTask(
    private val originalTask: ExternalTask,
    private val acquireAt: Instant,
) {
    fun toTask(): Task {
        return Task(
            activityId = originalTask.activityId,
            activityInstanceId = originalTask.activityInstanceId,
            errorMessage = originalTask.errorMessage,
            errorDetails = originalTask.errorDetails,
            executionId = originalTask.executionId,
            id = originalTask.id,
            lockExpirationTime = originalTask.lockExpirationTime,
            createTime = originalTask.createTime,
            processDefinitionId = originalTask.processDefinitionId,
            processDefinitionKey = originalTask.processDefinitionKey,
            processDefinitionVersionTag = originalTask.processDefinitionVersionTag,
            processInstanceId = originalTask.processInstanceId,
            retries = originalTask.retries,
            workerId = originalTask.workerId,
            topicName = originalTask.topicName,
            tenantId = originalTask.tenantId,
            priority = originalTask.priority,
            variables = originalTask.allVariables,
            businessKey = originalTask.businessKey,
            extensionProperties = originalTask.extensionProperties,
            acquireAt = acquireAt,
            originalTask = originalTask
        )
    }
}
