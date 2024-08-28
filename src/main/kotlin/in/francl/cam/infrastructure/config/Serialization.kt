package `in`.francl.cam.infrastructure.config

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json

fun Application.configureSerialization() {
    install(ContentNegotiation) {
//        gson {
//        }
//        jackson {
//            enable(SerializationFeature.INDENT_OUTPUT)
//        }
        json(Json {
            allowStructuredMapKeys = true
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
}
