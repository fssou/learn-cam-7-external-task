package `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda

import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.ext.CustomExternalTaskClientBuilderImpl
import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.ext.httpclient.interceptor.Logging
import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.ext.httpclient.interceptor.Measurement
import `in`.francl.cam.infrastructure.config.app.EnvironmentFactory.Type
import `in`.francl.cam.infrastructure.monitoring.instrumentation.Measurable
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy
import org.apache.hc.core5.util.TimeValue
import org.camunda.bpm.client.ExternalTaskClient
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

class ExternalTaskClientFactory(
    private val url: String,
    private val measurable: Measurable,
    private val type: Type = Type.DEFAULT,
) {

    fun create(): ExternalTaskClient {
        return when (type) {
            Type.DEFAULT -> createDefault()
        }
    }
    private fun createDefault() : ExternalTaskClient {
        return CustomExternalTaskClientBuilderImpl()
            .baseUrl(url)
            .maxTasks(50)
            .lockDuration(15.seconds.inWholeMilliseconds)
            .asyncResponseTimeout(15.seconds.inWholeMilliseconds)
            .usePriority(true)
            .orderByCreateTime().asc()
            .defaultSerializationFormat("application/x-java-serialized-object")
            .customizeHttpClient { it
                .setRetryStrategy(DefaultHttpRequestRetryStrategy(10, TimeValue.ofSeconds(1L)))
                .addExecInterceptorLast("Measurement", Measurement(measurable))
                .addExecInterceptorLast("Logging", Logging())
            }
            .build()
    }
    enum class Type {
        DEFAULT,
    }
    companion object {
        private val logger = LoggerFactory.getLogger(ExternalTaskClientFactory::class.java)!!
    }
}
