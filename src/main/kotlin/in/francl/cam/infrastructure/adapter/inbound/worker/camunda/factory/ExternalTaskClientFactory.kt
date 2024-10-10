package `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.factory

import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.ext.CustomExternalTaskClientBuilderImpl
import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.ext.httpclient.interceptor.Logging
import `in`.francl.cam.infrastructure.adapter.inbound.worker.camunda.ext.httpclient.interceptor.Measurement
import `in`.francl.cam.infrastructure.monitoring.measurement.Measurable
import net.logstash.logback.marker.Markers
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder
import org.apache.hc.core5.util.TimeValue
import org.camunda.bpm.client.ExternalTaskClient
import org.camunda.bpm.client.ExternalTaskClientBuilder
import org.camunda.bpm.client.backoff.ExponentialErrorBackoffStrategy
import org.slf4j.LoggerFactory
import java.util.function.Consumer
import kotlin.time.Duration.Companion.seconds

class ExternalTaskClientFactory(
    private val baseUrl: String,
    private val measurable: Measurable,
) {

    fun create(custom: ExternalTaskClientBuilder.() -> Unit = {}): ExternalTaskClient {
        val builder = createDefault()
        builder.apply(custom)
        val client = builder.build()
        val marker = Markers.append("client", client)!!
        logger.info(marker, "External task client created with custom configuration")
        return client
    }

    private fun createDefault(): ExternalTaskClientBuilder {

        val httpClientBuilder: Consumer<HttpClientBuilder> = Consumer { builder: HttpClientBuilder ->
            val loggingInterceptor = Logging()
            val measurementInterceptor = Measurement(measurable)
            val retryStrategy = DefaultHttpRequestRetryStrategy(10, TimeValue.ofSeconds(1L))
            builder.setRetryStrategy(retryStrategy)
            builder.addExecInterceptorLast("Measurement", measurementInterceptor)
            builder.addExecInterceptorLast("Logging", loggingInterceptor)
        }

        val maxTasks = 50
        val lockDuration = 15.seconds.inWholeMilliseconds
        val asyncResponseTimeout = 15.seconds.inWholeMilliseconds
        val usePriority = true
        val defaultSerializationFormat = "application/x-java-serialized-object"
        val backoffStrategy = ExponentialErrorBackoffStrategy(1000L, 2F, 30000L)

        return CustomExternalTaskClientBuilderImpl()
            .baseUrl(baseUrl)
            .maxTasks(maxTasks)
            .lockDuration(lockDuration)
            .asyncResponseTimeout(asyncResponseTimeout)
            .usePriority(usePriority)
            .defaultSerializationFormat(defaultSerializationFormat)
            .backoffStrategy(backoffStrategy)
            .customizeHttpClient(httpClientBuilder)
            .orderByCreateTime().asc()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ExternalTaskClientFactory::class.java)!!
    }
}
