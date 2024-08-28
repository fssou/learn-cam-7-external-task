package `in`.francl.cam.infrastructure.adapters.inbound.camunda

import `in`.francl.cam.application.annotation.TaskService
import `in`.francl.cam.domain.ports.inbound.task.TaskHandler
import `in`.francl.cam.domain.ports.outbound.task.TaskManager
import `in`.francl.cam.infrastructure.adapters.inbound.camunda.dto.CamundaExternalTask
import `in`.francl.cam.infrastructure.adapters.inbound.camunda.ext.CustomExternalTaskClientBuilderImpl
import `in`.francl.cam.infrastructure.monitoring.instrumentation.ExternalTaskClientHttpClientInstrumentation
import `in`.francl.cam.infrastructure.monitoring.instrumentation.TaskInstrumentation
import io.ktor.server.application.*
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.slf4j.MDCContext
import net.logstash.logback.marker.Markers
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy
import org.apache.hc.core5.util.TimeValue
import org.camunda.bpm.client.ExternalTaskClient
import org.koin.core.parameter.parametersOf
import org.koin.ktor.ext.get
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import java.time.Instant
import kotlin.time.Duration.Companion.seconds

fun Application.adapterInboundCamunda() {
    val logger = LoggerFactory.getLogger(::adapterInboundCamunda.javaClass)!!
    val url = environment.config.property("camunda.url").getString()
    val clients: MutableList<ExternalTaskClient> = mutableListOf()

    val taskHandlers = get<Set<TaskHandler>>()
    val externalTaskClientHttpClientInstrumentation by lazy { ExternalTaskClientHttpClientInstrumentation(get<PrometheusMeterRegistry>()) }
    val taskInstrumentation by lazy { TaskInstrumentation(get<PrometheusMeterRegistry>()) }

    val client = CustomExternalTaskClientBuilderImpl()
        .baseUrl(url)
        .maxTasks(50)
        .lockDuration(15.seconds.inWholeMilliseconds)
        .asyncResponseTimeout(15.seconds.inWholeMilliseconds)
        .usePriority(true)
        .orderByCreateTime().asc()
        .defaultSerializationFormat("application/x-java-serialized-object")
        .customizeHttpClient {
            it
                .setRetryStrategy(DefaultHttpRequestRetryStrategy(10, TimeValue.ofSeconds(1L)))
                .addExecInterceptorLast("Instrumentation") { request, scope, chain ->
                    externalTaskClientHttpClientInstrumentation.measureResponseTime(request) {
                        chain.proceed(request, scope)
                    }
                }
                .addResponseInterceptorLast { response, _, _ ->
                    when(response.code) {
                        !in 200..299 -> logger.error(Markers.append("http", response), "API camunda")
                    }
                }
        }
        .build()

    repeat(Runtime.getRuntime().availableProcessors()) {
        clients.add(client)
    }
    logger.info("Camunda clients created with ${clients.size} instances for ${taskHandlers.size} task handlers")

    taskHandlers
        .chunked(clients.size)
        .forEachIndexed { index, handlers ->
            handlers.forEach { handler ->
                val ann: TaskService = handler::class.annotations.filterIsInstance<TaskService>().first()
                clients[index]
                    .subscribe(ann.name)
                    .lockDuration(ann.lockDuration)
                    .localVariables(true)
                    .includeExtensionProperties(true)
                    .handler { externalTask, externalTaskService ->
                        val task = CamundaExternalTask(externalTask, Instant.now())
                        val service = get<TaskManager>{ parametersOf(externalTaskService, taskInstrumentation) }
                        taskInstrumentation.measureTaskAcquireTime(task)
                        hashMapOf(
                            "id" to task.id,
                            "retries" to task.retries?.toString(),
                            "workerId" to task.workerId,
                            "topicName" to task.topicName,
                            "activityId" to task.activityId,
                            "executionId" to task.executionId,
                            "businessKey" to task.businessKey,
                            "correlationId" to task.variables["correlationId"]?.toString(),
                            "processInstanceId" to task.processInstanceId,
                            "activityInstanceId" to task.activityInstanceId,
                            "processDefinitionId" to task.processDefinitionId,
                            "processDefinitionKey" to task.processDefinitionKey,
                            "processDefinitionVersionTag" to task.processDefinitionVersionTag,
                        ).forEach { (key, value) -> MDC.put(key, value) }
                        logger.info("Task handling")
                        CoroutineScope(Dispatchers.Default).launch(MDCContext()) {
                            logger.info("Task executing")
                            handler.execute(task, service)
                            logger.info("Task executed")
                        }
                        logger.info("Task handled")
                        MDC.clear()
                    }
                    .open()
            }
    }

}
