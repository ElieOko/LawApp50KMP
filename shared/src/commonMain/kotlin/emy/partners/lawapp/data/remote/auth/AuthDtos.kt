package emy.partners.lawapp.data.remote.auth

import kotlinx.serialization.Serializable

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
data class TokenPair(
    val accessToken: String? = null,
    val refreshToken: String? = null,
)

@Serializable
data class ApiMessage(
    val message: String? = null,
)

@Serializable
data class AuthUserProfile(
    val userId: Long? = null,
    val email: String? = null,
    val username: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val premium: Boolean? = null,
    val certified: Boolean? = null,
) {
    val displayName: String
        get() {
            val full = listOfNotNull(firstName?.takeIf { it.isNotBlank() }, lastName?.takeIf { it.isNotBlank() })
                .joinToString(" ")
            return when {
                full.isNotBlank() -> full
                !username.isNullOrBlank() -> username
                !email.isNullOrBlank() -> email
                else -> "Utilisateur LawApp"
            }
        }

    val displayHandle: String
        get() = when {
            !username.isNullOrBlank() -> "@$username"
            !email.isNullOrBlank() -> email
            else -> "@lawapp_member"
        }
}

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String? = null,
    val profile: AuthUserProfile? = null,
    val rawResponse: String = "",
)
