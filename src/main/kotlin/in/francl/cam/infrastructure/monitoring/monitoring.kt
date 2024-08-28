package `in`.francl.cam.infrastructure.monitoring

import io.ktor.server.application.*
import io.ktor.server.metrics.micrometer.*
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.koin

fun Application.monitoring() {
    koin {
        modules(
            module {
                single<PrometheusMeterRegistry> { PrometheusMeterRegistry(PrometheusConfig.DEFAULT) }
            }
        )
    }

    install(MicrometerMetrics) {
        registry = this@monitoring.get<PrometheusMeterRegistry>()
    }
}
