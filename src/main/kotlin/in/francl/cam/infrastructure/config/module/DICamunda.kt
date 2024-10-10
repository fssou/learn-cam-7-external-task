package `in`.francl.cam.infrastructure.config.module

import `in`.francl.cam.application.handler.TaskHandlerRegistry
import `in`.francl.cam.application.port.outbound.task.TaskManager
import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.factory.ExternalTaskClientFactory
import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.handler.DefaultExternalTaskHandler
import `in`.francl.cam.infrastructure.adapter.outbound.http.camunda.ExternalTaskServiceAdapter
import `in`.francl.cam.infrastructure.monitoring.measurement.ExternalTaskClientHttpClientMeasurement
import `in`.francl.cam.infrastructure.monitoring.measurement.TaskAcquisitionMeasurement
import `in`.francl.cam.infrastructure.monitoring.measurement.TaskExecutionMeasurement
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

            val baseUrlCamunda = environment.config.property("camunda.url").getString()

            val taskHandlerRegistry = get<TaskHandlerRegistry>()
            val meterRegistry = get<MeterRegistry>()

            val taskAcquisitionMeasurement = TaskAcquisitionMeasurement(meterRegistry)
            val taskExecutionMeasurement = TaskExecutionMeasurement(meterRegistry)
            val externalTaskClientHttpClientMeasurement = ExternalTaskClientHttpClientMeasurement(meterRegistry)

            val clientFactory = ExternalTaskClientFactory(baseUrlCamunda, externalTaskClientHttpClientMeasurement)

            val clients = mutableListOf<ExternalTaskClient>().apply {
                repeat(Runtime.getRuntime().availableProcessors()) {
                    add(clientFactory.create())
                }
            }

            logger.info("Camunda clients created with ${clients.size} instances for ${taskHandlerRegistry.handlers.size} task handlers")

            val taskManagerDelegate: (ExternalTaskService) -> `in`.francl.cam.application.port.outbound.task.TaskManager = {
                ExternalTaskServiceAdapter(it, taskExecutionMeasurement)
            }

            taskHandlerRegistry.handlers
                .forEach {
                    clients.forEach { client ->
                        val handler = it.first
                        val config = it.second
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