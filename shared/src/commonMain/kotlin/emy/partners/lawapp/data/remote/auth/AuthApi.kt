package emy.partners.lawapp.data.remote.auth

import emy.partners.lawapp.data.remote.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthApi(
    private val client: HttpClient = createHttpClient(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    },
) {
    suspend fun login(identifiant: String, password: String): Result<AuthSession> {
        return runCatching {
            val response = client.post("/auth/login") {
                setBody(UserAuthRequest(identifiant = identifiant.trim(), password = password))
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw AuthApiException(extractMessage(bodyText) ?: "Connexion impossible (${response.status.value})")
            }
            parseSession(bodyText)
        }
    }

    suspend fun register(request: UserRegisterRequest): Result<AuthSession> {
        return runCatching {
            val response = client.post("/auth/register") {
                setBody(request)
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw AuthApiException(extractMessage(bodyText) ?: "Inscription impossible (${response.status.value})")
            }
            // Some backends return a message-only body on register; treat as success without tokens.
            parseSessionOrEmpty(bodyText)
        }
    }

    private fun parseSession(bodyText: String): AuthSession {
        val session = parseSessionOrEmpty(bodyText)
        if (session.accessToken.isBlank()) {
            throw AuthApiException(extractMessage(bodyText) ?: "Reponse de connexion invalide")
        }
        return session
    }

    private fun parseSessionOrEmpty(bodyText: String): AuthSession {
        if (bodyText.isBlank()) {
            return AuthSession(accessToken = "", rawResponse = bodyText)
        }
        runCatching {
            val tokenPair = json.decodeFromString(TokenPair.serializer(), bodyText)
            if (!tokenPair.accessToken.isNullOrBlank()) {
                return AuthSession(
                    accessToken = tokenPair.accessToken,
                    refreshToken = tokenPair.refreshToken,
                    rawResponse = bodyText,
                )
            }
        }
        runCatching {
            val root = json.parseToJsonElement(bodyText).jsonObject
            val access = root["accessToken"]?.jsonPrimitive?.content
                ?: root["token"]?.jsonPrimitive?.content
                ?: root["data"]?.jsonObject?.get("accessToken")?.jsonPrimitive?.content
            val refresh = root["refreshToken"]?.jsonPrimitive?.content
                ?: root["data"]?.jsonObject?.get("refreshToken")?.jsonPrimitive?.content
            if (!access.isNullOrBlank()) {
                return AuthSession(
                    accessToken = access,
                    refreshToken = refresh,
                    rawResponse = bodyText,
                )
            }
        }
        return AuthSession(accessToken = "", rawResponse = bodyText)
    }

    private fun extractMessage(bodyText: String): String? {
        if (bodyText.isBlank()) return null
        return runCatching {
            json.decodeFromString(ApiMessage.serializer(), bodyText).message
        }.getOrNull() ?: runCatching {
            json.parseToJsonElement(bodyText).jsonObject["message"]?.jsonPrimitive?.content
        }.getOrNull()
    }
}

class AuthApiException(message: String) : Exception(message)

object AuthRepository {
    private val api = AuthApi()
    var currentSession: AuthSession? = null
        private set

    suspend fun login(identifiant: String, password: String): Result<AuthSession> {
        return api.login(identifiant, password).onSuccess { currentSession = it }
    }

    suspend fun register(request: UserRegisterRequest): Result<AuthSession> {
        return api.register(request).onSuccess { session ->
            if (session.accessToken.isNotBlank()) {
                currentSession = session
            }
        }
    }

    fun clearSession() {
        currentSession = null
    }
}
