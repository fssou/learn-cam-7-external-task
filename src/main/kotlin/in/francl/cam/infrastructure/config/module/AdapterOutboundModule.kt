package `in`.francl.cam.infrastructure.config.module

import `in`.francl.cam.domain.port.outbound.authorization.AuthorizationGateway
import `in`.francl.cam.domain.port.outbound.authorization.OAuth2Credentials
import `in`.francl.cam.domain.port.outbound.authorization.TokenExpirable
import `in`.francl.cam.domain.port.outbound.task.TaskManager
import `in`.francl.cam.infrastructure.adapter.outbound.authorization.SecurityTokenServiceAdapter
import `in`.francl.cam.infrastructure.adapter.outbound.camunda.ExternalTaskServiceAdapter
import `in`.francl.cam.infrastructure.cache.SimpleCache
import `in`.francl.cam.infrastructure.client.http.HttpClientFactory
import `in`.francl.cam.infrastructure.monitoring.instrumentation.HttpClientMeasurement
import io.ktor.server.application.*
import io.micrometer.core.instrument.MeterRegistry
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.koin
import org.slf4j.LoggerFactory

class AdapterOutboundModule : ModuleInitializer {
    override fun init(app: Application) {
        app.applyCamunda()
        app.applyAuthorization()
    }

    private fun Application.applyAuthorization() {
        val meterRegistry = get<MeterRegistry>()

        val url = environment.config.property("auth.url").getString()
        val oAuth2Credentials = OAuth2Credentials(
            clientId = environment.config.property("auth.clientId").getString(),
            clientSecret = environment.config.property("auth.clientSecret").getString(),
            grantType = environment.config.property("auth.grantType").getString(),
        )

        val measurement = HttpClientMeasurement(meterRegistry)
        val httpClient = HttpClientFactory(measurement).create()
        val cache = SimpleCache<Set<String>, TokenExpirable>()
        val authorizationGateway = SecurityTokenServiceAdapter(httpClient, url, oAuth2Credentials, cache)

        koin {
            modules(
                module {
                    single<AuthorizationGateway> { authorizationGateway }
                }
            )
        }
    }

    private fun Application.applyCamunda() {
        koin {
            modules(
                module {
                    single<TaskManager> { ExternalTaskServiceAdapter(it.get(), it.get()) }
                }
            )
        }
    }


    companion object {
        private val logger = LoggerFactory.getLogger(AdapterOutboundModule::class.java)!!
    }
}