package `in`.francl.cam.infrastructure.config.module

import `in`.francl.cam.application.port.outbound.authorization.AuthorizationGateway
import `in`.francl.cam.application.port.outbound.authorization.OAuth2Credentials
import `in`.francl.cam.application.port.outbound.authorization.TokenExpirable
import `in`.francl.cam.infrastructure.adapter.outbound.http.authorization.SecurityTokenServiceAdapter
import `in`.francl.cam.infrastructure.cache.SimpleCache
import `in`.francl.cam.infrastructure.client.http.HttpClientFactory
import `in`.francl.cam.infrastructure.monitoring.measurement.HttpClientMeasurement
import io.ktor.server.application.*
import io.micrometer.core.instrument.MeterRegistry
import org.koin.dsl.module
import org.koin.ktor.ext.get
import org.koin.ktor.plugin.koin

object DIAuthorization : (Application) -> Unit {
    override fun invoke(app: Application) {
        app.apply {
            val meterRegistry = get<MeterRegistry>()

            val url = environment.config.property("auth.url").getString()
            val oAuth2Credentials = `in`.francl.cam.application.port.outbound.authorization.OAuth2Credentials(
                clientId = environment.config.property("auth.clientId").getString(),
                clientSecret = environment.config.property("auth.clientSecret").getString(),
                grantType = environment.config.property("auth.grantType").getString(),
            )

            val measurement = HttpClientMeasurement(meterRegistry)
            val httpClient = HttpClientFactory(measurement).create()
            val cache = SimpleCache<Set<String>, `in`.francl.cam.application.port.outbound.authorization.TokenExpirable>()
            val authorizationGateway = SecurityTokenServiceAdapter(httpClient, url, oAuth2Credentials, cache)

            koin {
                modules(
                    module {
                        single<`in`.francl.cam.application.port.outbound.authorization.AuthorizationGateway> { authorizationGateway }
                    }
                )
            }
        }
    }
}