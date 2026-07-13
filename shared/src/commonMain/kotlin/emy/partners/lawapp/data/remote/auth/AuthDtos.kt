package emy.partners.lawapp.data.remote.auth

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class UserAuthRequest(
    val identifiant: String,
    val password: String,
)

@Serializable
data class UserRegisterRequest(
    val email: String,
    val password: String,
    val confirmPassword: String,
    val firstName: String,
    val lastName: String,
    val city: String,
    val pseudo: String? = null,
    val phone: String? = null,
)

@Serializable
data class IdentifiantRequest(
    val identifier: String,
)

@Serializable
data class VerifyRequest(
    val identifier: String,
    val code: String,
)

@Serializable
data class TokenPair(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val token: String? = null,
    @SerialName("refresh_token")
    val refreshTokenSnake: String? = null,
) {
    val resolvedAccessToken: String?
        get() = accessToken?.takeIf { it.isNotBlank() } ?: token?.takeIf { it.isNotBlank() }

    val resolvedRefreshToken: String?
        get() = refreshToken?.takeIf { it.isNotBlank() } ?: refreshTokenSnake?.takeIf { it.isNotBlank() }
}

@Serializable
data class ApiMessage(
    val message: String? = null,
)

@Serializable
data class LoginMemberPayload(
    val user: AuthUserProfile? = null,
    val profile: AuthUserProfile? = null,
)

@Serializable
data class LoginResponsePayload(
    val member: LoginMemberPayload? = null,
    val token: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    val accessToken: String? = null,
    val message: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class AuthUserProfile(
    val userId: Long? = null,
    val email: String? = null,
    val username: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    @JsonNames("isPremium")
    val premium: Boolean? = null,
    @JsonNames("isCertified")
    val certified: Boolean? = null,
    val accountId: Long? = null,
    val accountName: String? = null,
) {
    val displayName: String
        get() {
            val full = listOfNotNull(firstName?.takeIf { it.isNotBlank() }, lastName?.takeIf { it.isNotBlank() })
                .joinToString(" ")
            return when {
                full.isNotBlank() -> full
                !username.isNullOrBlank() -> username.trimStart('@')
                !email.isNullOrBlank() -> email
                else -> "Utilisateur LawApp"
            }
        }

    val displayHandle: String
        get() = when {
            !username.isNullOrBlank() -> if (username.startsWith("@")) username else "@$username"
            !email.isNullOrBlank() -> email
            else -> "@lawapp_member"
        }

    val fullName: String
        get() = listOfNotNull(
            firstName?.takeIf { it.isNotBlank() },
            lastName?.takeIf { it.isNotBlank() },
        ).joinToString(" ").ifBlank { displayName }

    val hasAccountType: Boolean
        get() = accountId != null || !accountName.isNullOrBlank()

    val accountTypeLabel: String
        get() = when (accountName?.lowercase()) {
            "student", "etudiant", "étudiant" -> "Etudiant"
            "teacher", "enseignant" -> "Enseignant"
            "defined" -> "Defini"
            else -> accountName?.replaceFirstChar { it.uppercase() } ?: "—"
        }
}

@Serializable
data class TypeAccountDto(
    val typeAccountId: Long? = null,
    val name: String? = null,
)

@Serializable
data class SelectableAccountDto(
    val id: Long,
    val name: String,
    val typeAccount: TypeAccountDto? = null,
) {
    val displayLabel: String
        get() = when (name.lowercase()) {
            "student" -> "Etudiant"
            "teacher" -> "Enseignant"
            else -> name.replaceFirstChar { it.uppercase() }
        }
}

@Serializable
data class SelectableAccountsResponse(
    val accounts: List<SelectableAccountDto> = emptyList(),
)

@Serializable
data class SelectAccountRequest(
    val userId: Long,
    val accountId: Long,
)

@Serializable
data class SelectedAccountLocal(
    val userId: Long,
    val accountId: Long,
    val accountName: String,
)

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String? = null,
    val profile: AuthUserProfile? = null,
    val rawResponse: String = "",
)
