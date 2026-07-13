package emy.partners.lawapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createHttpClient(): HttpClient = HttpClient(OkHttp) {
    expectSuccess = false
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            }
        )
    }
    defaultRequest {
        url(ApiConfig.BASE_URL)
        contentType(ContentType.Application.Json)
    }
}
