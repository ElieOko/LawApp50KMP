package emy.partners.lawapp.data.remote.contenu

import emy.partners.lawapp.data.remote.ApiConfig
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.data.remote.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ContenuApi(
    private val client: HttpClient = createHttpClient(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    },
) {
    suspend fun getPublicContenus(): Result<List<ContenuFeedItemDto>> {
        return runCatching {
            val response = client.get("/api/v1/public/contenu")
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw ContenuApiException(extractMessage(bodyText) ?: "Chargement des contenus impossible")
            }
            json.decodeFromString(ContenuListResponse.serializer(), bodyText).contenu
        }
    }

    suspend fun likeContenu(
        contenuId: Long,
        userId: Long,
        accessToken: String,
    ): Result<Unit> {
        return runCatching {
            val response = client.post {
                url.takeFrom("${ApiConfig.BASE_URL}/api/v1/private/like")
                withBearerToken(accessToken)
                contentType(ContentType.Application.Json)
                setBody(LikeContenuRequest(contenuId = contenuId, userId = userId))
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw ContenuApiException(
                    extractMessage(bodyText) ?: "Like impossible (${response.status.value})"
                )
            }
        }
    }

    private fun extractMessage(bodyText: String): String? {
        if (bodyText.isBlank()) return null
        return runCatching {
            json.parseToJsonElement(bodyText).jsonObject["message"]?.jsonPrimitive?.content
        }.getOrNull()
    }

    private fun HttpRequestBuilder.withBearerToken(token: String) {
        val clean = token.trim().removePrefix("Bearer").removePrefix("bearer").trim()
        require(clean.isNotBlank()) { "Token Bearer manquant" }
        headers.remove(HttpHeaders.Authorization)
        headers.append(HttpHeaders.Authorization, "Bearer $clean")
    }
}

class ContenuApiException(message: String) : Exception(message)

object ContenuRepository {
    private val api = ContenuApi()

    suspend fun loadHomeFeed(): Result<List<ContenuFeedItem>> {
        val currentUserId = AuthRepository.currentSession?.profile?.userId
        return api.getPublicContenus().map { items ->
            items.mapNotNull { it.toFeedItem(currentUserId) }
        }
    }

    suspend fun like(contenuId: Long): Result<Unit> {
        val session = AuthRepository.currentSession
            ?: return Result.failure(ContenuApiException("Connectez-vous pour liker"))
        val userId = session.profile?.userId
            ?: return Result.failure(ContenuApiException("Identifiant utilisateur manquant"))
        if (session.accessToken.isBlank()) {
            return Result.failure(ContenuApiException("Token manquant"))
        }
        return api.likeContenu(
            contenuId = contenuId,
            userId = userId,
            accessToken = session.accessToken,
        )
    }
}
