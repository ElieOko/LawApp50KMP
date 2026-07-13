package emy.partners.lawapp.data.remote.auth

import emy.partners.lawapp.data.local.LocalStore
import emy.partners.lawapp.data.local.createLocalStore
import emy.partners.lawapp.data.remote.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
            val session = parseSession(bodyText, fallbackIdentifiant = identifiant.trim())
            enrichSession(session)
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
            val session = parseSessionOrEmpty(
                bodyText = bodyText,
                fallbackProfile = AuthUserProfile(
                    email = identifier.trim().takeIf { it.contains("@") },
                    username = identifier.trim().takeUnless { it.contains("@") },
                )
            )
            if (session.accessToken.isNotBlank()) {
                enrichSession(session)
            } else {
                session
            }
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

    suspend fun fetchUserProfile(userId: Long, accessToken: String): AuthUserProfile? {
        return runCatching {
            val response = client.get("/api/v1/protected/users/$userId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) return null
            parseProfile(bodyText)
        }.getOrNull()
    }

    private suspend fun enrichSession(session: AuthSession): AuthSession {
        val jwtProfile = profileFromJwt(session.accessToken)
        var profile = mergeProfiles(session.profile, jwtProfile)

        val userId = profile?.userId ?: jwtProfile?.userId
        if (userId != null && session.accessToken.isNotBlank()) {
            val remote = fetchUserProfile(userId, session.accessToken)
            profile = mergeProfiles(profile, remote)
        }

        return session.copy(profile = profile)
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

        var access: String? = null
        var refresh: String? = null
        var profile: AuthUserProfile? = null

        runCatching {
            val tokenPair = json.decodeFromString(TokenPair.serializer(), bodyText)
            access = tokenPair.accessToken
            refresh = tokenPair.refreshToken
        }

        runCatching {
            val root = json.parseToJsonElement(bodyText).jsonObject
            if (access.isNullOrBlank()) {
                val dataObject = root["data"]?.jsonObject
                access = root.stringOrNull("accessToken")
                    ?: root.stringOrNull("token")
                    ?: dataObject?.stringOrNull("accessToken")
                refresh = root.stringOrNull("refreshToken")
                    ?: dataObject?.stringOrNull("refreshToken")
            }
            profile = parseProfileFromRoot(root)
        }

        return AuthSession(
            accessToken = access.orEmpty(),
            refreshToken = refresh,
            profile = mergeProfiles(fallbackProfile, profile),
            rawResponse = bodyText,
        )
    }

    private fun parseProfile(bodyText: String): AuthUserProfile? {
        if (bodyText.isBlank()) return null
        runCatching {
            return json.decodeFromString(AuthUserProfile.serializer(), bodyText)
        }
        return runCatching {
            parseProfileFromRoot(json.parseToJsonElement(bodyText).jsonObject)
        }.getOrNull()
    }

    private fun parseProfileFromRoot(root: JsonObject): AuthUserProfile? {
        val dataObject = root["data"]?.jsonObject
        val candidates = listOfNotNull(
            root["user"]?.jsonObject,
            root["profile"]?.jsonObject,
            dataObject?.get("user")?.jsonObject,
            dataObject?.get("profile")?.jsonObject,
            dataObject,
            root.takeIf { it.containsKey("email") || it.containsKey("username") || it.containsKey("userId") },
        )
        for (candidate in candidates) {
            val decoded = runCatching {
                json.decodeFromJsonElement(AuthUserProfile.serializer(), candidate)
            }.getOrNull()
            if (decoded != null && !decoded.isEmpty()) return decoded
            val mapped = mapProfileObject(candidate)
            if (mapped != null && !mapped.isEmpty()) return mapped
        }
        return null
    }

    private fun mapProfileObject(obj: JsonObject): AuthUserProfile? {
        val profile = AuthUserProfile(
            userId = obj.longOrNull("userId") ?: obj.longOrNull("id"),
            email = obj.stringOrNull("email"),
            username = obj.stringOrNull("username") ?: obj.stringOrNull("pseudo"),
            phone = obj.stringOrNull("phone") ?: obj.stringOrNull("telephone"),
            city = obj.stringOrNull("city") ?: obj.stringOrNull("ville"),
            firstName = obj.stringOrNull("firstName") ?: obj.stringOrNull("nom"),
            lastName = obj.stringOrNull("lastName") ?: obj.stringOrNull("prenom"),
            premium = obj.booleanOrNull("premium"),
            certified = obj.booleanOrNull("certified"),
        )
        return profile.takeUnless { it.isEmpty() }
    }

    @OptIn(ExperimentalEncodingApi::class)
    private fun profileFromJwt(accessToken: String): AuthUserProfile? {
        val parts = accessToken.split(".")
        if (parts.size < 2) return null
        return runCatching {
            val payload = parts[1]
                .replace('-', '+')
                .replace('_', '/')
                .let { value ->
                    val padding = (4 - value.length % 4) % 4
                    value + "=".repeat(padding)
                }
            val decoded = Base64.decode(payload).decodeToString()
            val obj = json.parseToJsonElement(decoded).jsonObject
            AuthUserProfile(
                userId = obj.longOrNull("userId")
                    ?: obj.longOrNull("id")
                    ?: obj.stringOrNull("sub")?.toLongOrNull(),
                email = obj.stringOrNull("email"),
                username = obj.stringOrNull("username") ?: obj.stringOrNull("pseudo"),
                phone = obj.stringOrNull("phone"),
                city = obj.stringOrNull("city"),
                firstName = obj.stringOrNull("firstName"),
                lastName = obj.stringOrNull("lastName"),
                premium = obj.booleanOrNull("premium"),
                certified = obj.booleanOrNull("certified"),
            ).takeUnless { it.isEmpty() }
        }.getOrNull()
    }

    private fun extractMessage(bodyText: String): String? {
        if (bodyText.isBlank()) return null
        return runCatching {
            json.decodeFromString(ApiMessage.serializer(), bodyText).message
        }.getOrNull() ?: runCatching {
            json.parseToJsonElement(bodyText).jsonObject.stringOrNull("message")
        }.getOrNull()
    }
}

class AuthApiException(message: String) : Exception(message)

object AuthRepository {
    private const val KEY_SESSION = "lawapp_auth_session"
    private const val KEY_PROFILE = "lawapp_auth_profile"

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
            // Ne pas considerer l'utilisateur connecte tant qu'il n'a pas de token.
            if (session.accessToken.isNotBlank()) {
                persistSession(session)
            } else {
                // Conserve le profil saisi localement en attendant OTP / login.
                session.profile?.let { persistProfileOnly(it) }
            }
        }
    }

    suspend fun validateOtp(identifier: String, code: String): Result<AuthSession> {
        return api.validateOtp(identifier, code).onSuccess { session ->
            if (session.accessToken.isNotBlank()) {
                val merged = session.copy(
                    profile = mergeProfiles(loadProfileOnly(), session.profile)
                )
                persistSession(merged)
            }
        }
    }

    suspend fun generateOtp(identifier: String): Result<Unit> {
        return api.generateOtp(identifier)
    }

    fun clearSession() {
        currentSession = null
        store.remove(KEY_SESSION)
        store.remove(KEY_PROFILE)
    }

    private fun persistSession(session: AuthSession) {
        val previous = loadProfileOnly()?.takeIf { saved ->
            val email = session.profile?.email
            val userId = session.profile?.userId
            when {
                userId != null && saved.userId != null -> saved.userId == userId
                !email.isNullOrBlank() && !saved.email.isNullOrBlank() ->
                    saved.email.equals(email, ignoreCase = true)
                else -> true
            }
        }
        val mergedProfile = mergeProfiles(previous, session.profile)
        val toStore = session.copy(profile = mergedProfile)
        currentSession = toStore
        store.putString(KEY_SESSION, json.encodeToString(AuthSession.serializer(), toStore))
        mergedProfile?.let { persistProfileOnly(it) }
    }

    private fun persistProfileOnly(profile: AuthUserProfile) {
        store.putString(KEY_PROFILE, json.encodeToString(AuthUserProfile.serializer(), profile))
    }

    private fun loadProfileOnly(): AuthUserProfile? {
        val raw = store.getString(KEY_PROFILE) ?: return null
        return runCatching {
            json.decodeFromString(AuthUserProfile.serializer(), raw)
        }.getOrNull()
    }

    private fun loadSession(): AuthSession? {
        val raw = store.getString(KEY_SESSION) ?: return null
        return runCatching {
            val session = json.decodeFromString(AuthSession.serializer(), raw)
            val profile = mergeProfiles(loadProfileOnly(), session.profile)
            session.copy(profile = profile)
        }.getOrNull()
    }
}

internal fun mergeProfiles(
    base: AuthUserProfile?,
    overlay: AuthUserProfile?,
): AuthUserProfile? {
    if (base == null) return overlay
    if (overlay == null) return base
    return AuthUserProfile(
        userId = overlay.userId ?: base.userId,
        email = overlay.email?.takeIf { it.isNotBlank() } ?: base.email,
        username = overlay.username?.takeIf { it.isNotBlank() } ?: base.username,
        phone = overlay.phone?.takeIf { it.isNotBlank() } ?: base.phone,
        city = overlay.city?.takeIf { it.isNotBlank() } ?: base.city,
        firstName = overlay.firstName?.takeIf { it.isNotBlank() } ?: base.firstName,
        lastName = overlay.lastName?.takeIf { it.isNotBlank() } ?: base.lastName,
        premium = overlay.premium ?: base.premium,
        certified = overlay.certified ?: base.certified,
    )
}

private fun AuthUserProfile.isEmpty(): Boolean =
    userId == null &&
        email.isNullOrBlank() &&
        username.isNullOrBlank() &&
        phone.isNullOrBlank() &&
        city.isNullOrBlank() &&
        firstName.isNullOrBlank() &&
        lastName.isNullOrBlank() &&
        premium == null &&
        certified == null

private fun JsonObject.stringOrNull(key: String): String? =
    this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

private fun JsonObject.longOrNull(key: String): Long? {
    val primitive = this[key]?.jsonPrimitive ?: return null
    return primitive.longOrNull ?: primitive.contentOrNull?.toLongOrNull()
}

private fun JsonObject.booleanOrNull(key: String): Boolean? {
    val primitive = this[key]?.jsonPrimitive ?: return null
    return primitive.booleanOrNull ?: primitive.contentOrNull?.toBooleanStrictOrNull()
}
