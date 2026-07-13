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

data class AuthSession(
    val accessToken: String,
    val refreshToken: String? = null,
    val rawResponse: String = "",
)
