package `in`.francl.cam.infrastructure.adapters.outbound.hc.interceptors

import `in`.francl.cam.infrastructure.monitoring.instrumentation.HttpClientInstrumentation
import okhttp3.Interceptor
import okhttp3.Response

class InstrumentationInterceptor(
    private val instrumentation: HttpClientInstrumentation
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return instrumentation.measureResponseTime {
            chain.proceed(chain.request())
        }
    }
}