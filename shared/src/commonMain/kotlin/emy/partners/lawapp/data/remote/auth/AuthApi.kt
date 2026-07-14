package emy.partners.lawapp.data.remote.auth

import emy.partners.lawapp.data.local.LocalStore
import emy.partners.lawapp.data.local.createLocalStore
import emy.partners.lawapp.data.remote.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.put
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

    suspend fun getSelectableAccounts(): Result<List<SelectableAccountDto>> {
        return runCatching {
            val response = client.get("/api/v1/public/accounts/selectable")
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw AuthApiException(extractMessage(bodyText) ?: "Impossible de charger les types de compte")
            }
            json.decodeFromString(SelectableAccountsResponse.serializer(), bodyText).accounts
        }
    }

    suspend fun selectAccount(userId: Long, accountId: Long): Result<SelectAccountOutcome> {
        return runCatching {
            val response = client.post("/api/v1/public/accounts/selectable") {
                setBody(SelectAccountRequest(userId = userId, accountId = accountId))
            }
            val bodyText = response.bodyAsText()
            val message = extractMessage(bodyText).orEmpty()
            val normalized = message.lowercase()
            when {
                !response.status.isSuccess() -> {
                    throw AuthApiException(message.ifBlank { "Selection du compte impossible (${response.status.value})" })
                }
                normalized.contains("déjà sélectionné") || normalized.contains("deja selectionne") ->
                    SelectAccountOutcome.AlreadySelectedOther
                normalized.contains("déjà associé") || normalized.contains("deja associe") ->
                    SelectAccountOutcome.AlreadySelectedSame
                else -> SelectAccountOutcome.Created
            }
        }
    }

    suspend fun updateUser(userId: Long, accessToken: String, request: UserRequestChange): Result<AuthUserProfile> {
        return runCatching {
            val response = client.put("/api/v1/protected/users/$userId") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                setBody(request)
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw AuthApiException(extractMessage(bodyText) ?: "Mise a jour du profil impossible (${response.status.value})")
            }
            parseProfile(bodyText) ?: AuthUserProfile(
                userId = userId,
                email = request.email,
                username = request.pseudo,
                phone = request.phone,
                city = request.city,
                firstName = request.firstName,
                lastName = request.lastName,
            )
        }
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

        // Structure reelle login:
        // { member: { user: {...}, profile }, token, refresh_token, message }
        runCatching {
            val login = json.decodeFromString(LoginResponsePayload.serializer(), bodyText)
            access = login.token?.takeIf { it.isNotBlank() } ?: login.accessToken?.takeIf { it.isNotBlank() }
            refresh = login.refreshToken?.takeIf { it.isNotBlank() }
            profile = mergeProfiles(login.member?.user, login.member?.profile)
        }

        runCatching {
            val tokenPair = json.decodeFromString(TokenPair.serializer(), bodyText)
            if (access.isNullOrBlank()) access = tokenPair.resolvedAccessToken
            if (refresh.isNullOrBlank()) refresh = tokenPair.resolvedRefreshToken
        }

        runCatching {
            val root = json.parseToJsonElement(bodyText).jsonObject
            if (access.isNullOrBlank()) {
                val dataObject = root["data"]?.jsonObject
                access = root.stringOrNull("token")
                    ?: root.stringOrNull("accessToken")
                    ?: dataObject?.stringOrNull("token")
                    ?: dataObject?.stringOrNull("accessToken")
            }
            if (refresh.isNullOrBlank()) {
                val dataObject = root["data"]?.jsonObject
                refresh = root.stringOrNull("refresh_token")
                    ?: root.stringOrNull("refreshToken")
                    ?: dataObject?.stringOrNull("refresh_token")
                    ?: dataObject?.stringOrNull("refreshToken")
            }
            profile = mergeProfiles(profile, parseProfileFromRoot(root))
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
        val memberObject = root["member"]?.jsonObject
        val candidates = listOfNotNull(
            memberObject?.get("user")?.jsonObject,
            memberObject?.get("profile")?.jsonObject,
            root["user"]?.jsonObject,
            root["profile"]?.jsonObject,
            dataObject?.get("user")?.jsonObject,
            dataObject?.get("profile")?.jsonObject,
            dataObject?.get("member")?.jsonObject?.get("user")?.jsonObject,
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
            premium = obj.booleanOrNull("isPremium") ?: obj.booleanOrNull("premium"),
            certified = obj.booleanOrNull("isCertified") ?: obj.booleanOrNull("certified"),
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
            val sub = obj.stringOrNull("sub")
            AuthUserProfile(
                userId = obj.longOrNull("userId")
                    ?: obj.longOrNull("id")
                    ?: sub?.toLongOrNull()
                    ?: sub?.toLongOrNull(radix = 16),
                email = obj.stringOrNull("email"),
                username = obj.stringOrNull("username") ?: obj.stringOrNull("pseudo"),
                phone = obj.stringOrNull("phone"),
                city = obj.stringOrNull("city"),
                firstName = obj.stringOrNull("firstName"),
                lastName = obj.stringOrNull("lastName"),
                premium = obj.booleanOrNull("isPremium") ?: obj.booleanOrNull("premium"),
                certified = obj.booleanOrNull("isCertified") ?: obj.booleanOrNull("certified"),
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

enum class SelectAccountOutcome {
    Created,
    AlreadySelectedSame,
    AlreadySelectedOther,
}

class AuthApiException(message: String) : Exception(message)

object AuthRepository {
    private const val KEY_SESSION = "lawapp_auth_session"
    private const val KEY_PROFILE = "lawapp_auth_profile"
    private const val KEY_SELECTED_ACCOUNT_PREFIX = "lawapp_selected_account_"

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

    suspend fun getSelectableAccounts(): Result<List<SelectableAccountDto>> {
        return api.getSelectableAccounts()
    }

    suspend fun refreshProfile(): Result<AuthSession> {
        val session = currentSession
            ?: return Result.failure(AuthApiException("Session absente"))
        val userId = session.profile?.userId
            ?: return Result.failure(AuthApiException("Identifiant utilisateur manquant"))
        if (session.accessToken.isBlank()) {
            return Result.failure(AuthApiException("Token manquant"))
        }
        return runCatching {
            val remote = api.fetchUserProfile(userId, session.accessToken)
                ?: throw AuthApiException("Impossible de rafraichir le profil")
            val merged = session.copy(
                profile = mergeProfiles(session.profile, remote)?.copy(
                    avatarUri = session.profile?.avatarUri,
                    accountId = session.profile?.accountId ?: remote.accountId,
                    accountName = session.profile?.accountName ?: remote.accountName,
                )
            )
            persistSession(merged)
            merged
        }
    }

    suspend fun updateProfile(request: UserRequestChange): Result<AuthSession> {
        val session = currentSession
            ?: return Result.failure(AuthApiException("Session absente"))
        val userId = session.profile?.userId
            ?: return Result.failure(AuthApiException("Identifiant utilisateur manquant"))
        if (session.accessToken.isBlank()) {
            return Result.failure(AuthApiException("Token manquant"))
        }
        return api.updateUser(userId, session.accessToken, request).map { updated ->
            val merged = session.copy(
                profile = mergeProfiles(session.profile, updated)?.copy(
                    avatarUri = session.profile?.avatarUri,
                    accountId = session.profile?.accountId,
                    accountName = session.profile?.accountName,
                    email = request.email,
                    username = request.pseudo,
                    phone = request.phone,
                    city = request.city,
                    firstName = request.firstName,
                    lastName = request.lastName,
                )
            )
            persistSession(merged)
            merged
        }
    }

    fun updateAvatarUri(uri: String): AuthSession? {
        val session = currentSession ?: return null
        val profile = (session.profile ?: AuthUserProfile()).copy(avatarUri = uri)
        val updated = session.copy(profile = profile)
        persistSession(updated)
        return updated
    }

    suspend fun selectAccountType(account: SelectableAccountDto): Result<AuthSession> {
        val session = currentSession
            ?: return Result.failure(AuthApiException("Connectez-vous pour choisir un type de compte"))
        val userId = session.profile?.userId
            ?: return Result.failure(AuthApiException("Identifiant utilisateur manquant"))

        val existingLocal = loadSelectedAccount(userId)
            ?: session.profile?.takeIf { it.hasAccountType }?.let { profile ->
                SelectedAccountLocal(
                    userId = userId,
                    accountId = profile.accountId ?: 0L,
                    accountName = profile.accountName ?: "defined",
                )
            }
        if (existingLocal != null) {
            return Result.success(applySelectedAccount(session, existingLocal))
        }

        return api.selectAccount(userId = userId, accountId = account.id).map { outcome ->
            when (outcome) {
                SelectAccountOutcome.Created,
                SelectAccountOutcome.AlreadySelectedSame -> {
                    applySelectedAccount(
                        session,
                        SelectedAccountLocal(
                            userId = userId,
                            accountId = account.id,
                            accountName = account.name,
                        )
                    )
                }
                SelectAccountOutcome.AlreadySelectedOther -> {
                    applySelectedAccount(
                        session,
                        SelectedAccountLocal(
                            userId = userId,
                            accountId = 0L,
                            accountName = "defined",
                        )
                    )
                }
            }
        }
    }

    fun clearSession() {
        currentSession = null
        store.remove(KEY_SESSION)
        store.remove(KEY_PROFILE)
    }

    private fun applySelectedAccount(
        session: AuthSession,
        selected: SelectedAccountLocal,
    ): AuthSession {
        persistSelectedAccount(selected)
        val profile = (session.profile ?: AuthUserProfile(userId = selected.userId)).copy(
            accountId = selected.accountId,
            accountName = selected.accountName,
        )
        val updated = session.copy(profile = profile)
        persistSession(updated)
        return updated
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
        var mergedProfile = mergeProfiles(previous, session.profile)
        val userId = mergedProfile?.userId
        if (userId != null) {
            val selected = loadSelectedAccount(userId)
            if (selected != null) {
                mergedProfile = (mergedProfile ?: AuthUserProfile(userId = userId)).copy(
                    accountId = selected.accountId,
                    accountName = selected.accountName,
                )
            }
        }
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

    private fun persistSelectedAccount(selected: SelectedAccountLocal) {
        store.putString(
            KEY_SELECTED_ACCOUNT_PREFIX + selected.userId,
            json.encodeToString(SelectedAccountLocal.serializer(), selected),
        )
    }

    private fun loadSelectedAccount(userId: Long): SelectedAccountLocal? {
        val raw = store.getString(KEY_SELECTED_ACCOUNT_PREFIX + userId) ?: return null
        return runCatching {
            json.decodeFromString(SelectedAccountLocal.serializer(), raw)
        }.getOrNull()
    }

    private fun loadSession(): AuthSession? {
        val raw = store.getString(KEY_SESSION) ?: return null
        return runCatching {
            val session = json.decodeFromString(AuthSession.serializer(), raw)
            var profile = mergeProfiles(loadProfileOnly(), session.profile)
            val userId = profile?.userId
            if (userId != null) {
                val selected = loadSelectedAccount(userId)
                if (selected != null) {
                    profile = (profile ?: AuthUserProfile(userId = userId)).copy(
                        accountId = selected.accountId,
                        accountName = selected.accountName,
                    )
                }
            }
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
        accountId = overlay.accountId ?: base.accountId,
        accountName = overlay.accountName?.takeIf { it.isNotBlank() } ?: base.accountName,
        avatarUri = overlay.avatarUri?.takeIf { it.isNotBlank() } ?: base.avatarUri,
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
        certified == null &&
        accountId == null &&
        accountName.isNullOrBlank() &&
        avatarUri.isNullOrBlank()

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
