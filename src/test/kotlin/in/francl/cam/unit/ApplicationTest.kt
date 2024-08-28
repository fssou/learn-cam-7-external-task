package `in`.francl.cam.unit

import `in`.francl.cam.infrastructure.infrastructure
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test

class ApplicationTest {
    @Test
    fun test() = testApplication {
        externalServices {
            hosts("http://localhost:58080") {
                routing {
                    get("/engine-rest/fetchAndLock") {
                        call.respondText("""[{"id":"1","topicName":"tests","workerId":"worker","lockDuration":10000,"variables":{}}]""")
                    }
                }
            }
        }
        application {
            infrastructure()
        }
        client.get("/monitoring/health").let { response ->
            assert(response.status.isSuccess())
        }
    }
}