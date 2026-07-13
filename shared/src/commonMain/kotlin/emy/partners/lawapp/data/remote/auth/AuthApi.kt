package emy.partners.lawapp.data.remote.auth

import emy.partners.lawapp.data.local.LocalStore
import emy.partners.lawapp.data.local.createLocalStore
import emy.partners.lawapp.data.remote.createHttpClient
import io.ktor.client.HttpClient
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
            parseSession(bodyText, fallbackIdentifiant = identifiant.trim())
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
            parseSessionOrEmpty(
                bodyText = bodyText,
                fallbackProfile = AuthUserProfile(
                    email = request.email,
                    username = request.pseudo,
                    phone = request.phone,
                    city = request.city,
                    firstName = request.firstName,
                    lastName = request.lastName,
                )
            )
        }
    }

    suspend fun validateOtp(identifier: String, code: String): Result<AuthSession> {
        return runCatching {
            val response = client.post("/api/v1/public/otp/validate") {
                setBody(
                    VerifyRequest(
                        identifier = identifier.trim(),
                        code = code.trim(),
                    )
                )
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw AuthApiException(extractMessage(bodyText) ?: "Code OTP invalide (${response.status.value})")
            }
            parseSessionOrEmpty(
                bodyText = bodyText,
                fallbackProfile = AuthUserProfile(
                    email = identifier.trim().takeIf { it.contains("@") },
                    username = identifier.trim().takeUnless { it.contains("@") },
                )
            )
        }
    }

    suspend fun generateOtp(identifier: String): Result<Unit> {
        return runCatching {
            val response = client.post("/api/v1/public/otp/generate") {
                setBody(IdentifiantRequest(identifier = identifier.trim()))
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw AuthApiException(extractMessage(bodyText) ?: "Renvoi du code impossible (${response.status.value})")
            }
        }
    }

    private fun parseSession(bodyText: String, fallbackIdentifiant: String): AuthSession {
        val session = parseSessionOrEmpty(
            bodyText = bodyText,
            fallbackProfile = AuthUserProfile(
                email = fallbackIdentifiant.takeIf { it.contains("@") },
                username = fallbackIdentifiant.takeUnless { it.contains("@") },
            )
        )
        if (session.accessToken.isBlank()) {
            throw AuthApiException(extractMessage(bodyText) ?: "Reponse de connexion invalide")
        }
        return session
    }

    private fun parseSessionOrEmpty(
        bodyText: String,
        fallbackProfile: AuthUserProfile? = null,
    ): AuthSession {
        if (bodyText.isBlank()) {
            return AuthSession(accessToken = "", profile = fallbackProfile, rawResponse = bodyText)
        }

        runCatching {
            val tokenPair = json.decodeFromString(TokenPair.serializer(), bodyText)
            if (!tokenPair.accessToken.isNullOrBlank()) {
                return AuthSession(
                    accessToken = tokenPair.accessToken,
                    refreshToken = tokenPair.refreshToken,
                    profile = fallbackProfile,
                    rawResponse = bodyText,
                )
            }
        }

        runCatching {
            val root = json.parseToJsonElement(bodyText).jsonObject
            val dataObject = root["data"]?.jsonObject
            val access = root["accessToken"]?.jsonPrimitive?.content
                ?: root["token"]?.jsonPrimitive?.content
                ?: dataObject?.get("accessToken")?.jsonPrimitive?.content
            val refresh = root["refreshToken"]?.jsonPrimitive?.content
                ?: dataObject?.get("refreshToken")?.jsonPrimitive?.content

            val profile = runCatching {
                val profileElement = root["user"]
                    ?: root["profile"]
                    ?: dataObject?.get("user")
                    ?: dataObject?.get("profile")
                    ?: dataObject
                profileElement?.let {
                    json.decodeFromJsonElement(AuthUserProfile.serializer(), it)
                }
            }.getOrNull() ?: fallbackProfile

            if (!access.isNullOrBlank()) {
                return AuthSession(
                    accessToken = access,
                    refreshToken = refresh,
                    profile = profile,
                    rawResponse = bodyText,
                )
            }
        }

        return AuthSession(
            accessToken = "",
            profile = fallbackProfile,
            rawResponse = bodyText,
        )
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
    private const val KEY_SESSION = "lawapp_auth_session"

    private val api = AuthApi()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }

    private val store: LocalStore by lazy { createLocalStore() }

    var currentSession: AuthSession? = null
        private set

    init {
        currentSession = loadSession()
    }

    suspend fun login(identifiant: String, password: String): Result<AuthSession> {
        return api.login(identifiant, password).onSuccess { session ->
            persistSession(session)
        }
    }

    suspend fun register(request: UserRegisterRequest): Result<AuthSession> {
        return api.register(request).onSuccess { session ->
            if (session.accessToken.isNotBlank()) {
                persistSession(session)
            } else if (session.profile != null) {
                // Keep profile even if backend returns no token yet.
                persistSession(session.copy(accessToken = currentSession?.accessToken.orEmpty()))
            }
        }
    }

    suspend fun validateOtp(identifier: String, code: String): Result<AuthSession> {
        return api.validateOtp(identifier, code).onSuccess { session ->
            if (session.accessToken.isNotBlank()) {
                persistSession(session)
            }
        }
    }

    suspend fun generateOtp(identifier: String): Result<Unit> {
        return api.generateOtp(identifier)
    }

    fun clearSession() {
        currentSession = null
        store.remove(KEY_SESSION)
    }

    private fun persistSession(session: AuthSession) {
        currentSession = session
        store.putString(KEY_SESSION, json.encodeToString(AuthSession.serializer(), session))
    }

    private fun loadSession(): AuthSession? {
        val raw = store.getString(KEY_SESSION) ?: return null
        return runCatching {
            json.decodeFromString(AuthSession.serializer(), raw)
        }.getOrNull()
    }
}
