package `in`.francl.cam.infrastructure.adapters.inbound.camunda.dto

import `in`.francl.cam.domain.ports.inbound.task.Task
import org.camunda.bpm.client.task.ExternalTask
import java.time.Instant

class CamundaExternalTask(
    private val originalTask: ExternalTask,
    acquireAt: Instant,
) : Task(
    originalTask.activityId,
    originalTask.activityInstanceId,
    originalTask.errorMessage,
    originalTask.errorDetails,
    originalTask.executionId,
    originalTask.id,
    originalTask.lockExpirationTime,
    originalTask.createTime,
    originalTask.processDefinitionId,
    originalTask.processDefinitionKey,
    originalTask.processDefinitionVersionTag,
    originalTask.processInstanceId,
    originalTask.retries,
    originalTask.workerId,
    originalTask.topicName,
    originalTask.tenantId,
    originalTask.priority,
    originalTask.allVariables,
    originalTask.businessKey,
    originalTask.extensionProperties,
    acquireAt,
) {
    override fun <T> getOriginalTask(): T where T: Any {
        return originalTask as T
    }
}
