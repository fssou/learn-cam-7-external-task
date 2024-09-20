package `in`.francl.cam.infrastructure.config.module

import `in`.francl.cam.application.handler.Configurable
import `in`.francl.cam.application.handler.DefaultHandlerConfig
import `in`.francl.cam.domain.port.inbound.task.TaskHandler
import `in`.francl.cam.domain.port.outbound.task.TaskManager
import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.DefaultExternalTaskHandler
import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.ExternalTaskClientFactory
import `in`.francl.cam.infrastructure.adapter.outbound.camunda.ExternalTaskServiceAdapter
import `in`.francl.cam.infrastructure.monitoring.instrumentation.ExternalTaskClientHttpClientMeasurement
import `in`.francl.cam.infrastructure.monitoring.instrumentation.TaskAcquisitionMeasurement
import `in`.francl.cam.infrastructure.monitoring.instrumentation.TaskExecutionMeasurement
import io.ktor.server.application.*
import io.micrometer.core.instrument.MeterRegistry
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.task.ExternalTaskService
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.koin
import org.slf4j.LoggerFactory

object DICamunda : (Application) -> Unit {
    private val logger = LoggerFactory.getLogger(DICamunda::class.java)!!
    override fun invoke(app: Application) {
        app.apply {

            val url = environment.config.property("camunda.url").getString()
            val clients: MutableList<ExternalTaskClient> = mutableListOf()

            val taskHandlers: Set<TaskHandler> = setOf()
            val meterRegistry = get<MeterRegistry>()
            val externalTaskClientHttpClientMeasurement = ExternalTaskClientHttpClientMeasurement(meterRegistry)

            val clientFactory = ExternalTaskClientFactory(url, externalTaskClientHttpClientMeasurement)

            repeat(Runtime.getRuntime().availableProcessors()) {
                clients.add(clientFactory.create())
            }
            logger.info("Camunda clients created with ${clients.size} instances for ${taskHandlers.size} task handlers")

            val taskAcquisitionMeasurement = TaskAcquisitionMeasurement(meterRegistry)
            val taskExecutionMeasurement = TaskExecutionMeasurement(meterRegistry)

            val taskManagerDelegate: (ExternalTaskService) -> TaskManager = {
                ExternalTaskServiceAdapter(it, taskExecutionMeasurement)
            }

            taskHandlers
                .forEach { handler ->
                    clients.forEach { client ->
                        require(handler is Configurable<*>)
                        require(handler.config is DefaultHandlerConfig)
                        val config = handler.config as DefaultHandlerConfig
                        val topicName = config.topicName
                        val lockDuration = config.lockDuration
                        val handlerBase = DefaultExternalTaskHandler(handler, taskManagerDelegate, taskAcquisitionMeasurement)
                        client
                            .subscribe(topicName)
                            .lockDuration(lockDuration)
                            .localVariables(true)
                            .includeExtensionProperties(true)
                            .handler(handlerBase)
                            .open()
                    }
                }

            koin {
                modules(
                    module {

                    }
                )
            }
        }
    }
}