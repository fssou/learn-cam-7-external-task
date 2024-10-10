package `in`.francl.cam.infrastructure.config.module

import io.ktor.server.application.*
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.koin.dsl.module
import org.koin.ktor.plugin.koin

object DIMetrics : (Application) -> Unit {
    override fun invoke(app: Application) {
        app.apply {
            val meterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

            koin {
                modules(
                    module {
                        single<MeterRegistry> { meterRegistry }
                    }
                )
            }
        }
    }
}