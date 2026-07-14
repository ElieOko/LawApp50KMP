package emy.partners.lawapp.data.remote.contenu

import emy.partners.lawapp.data.local.LocalStore
import emy.partners.lawapp.data.local.createLocalStore
import emy.partners.lawapp.data.remote.ApiConfig
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.data.remote.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
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

    suspend fun createContenu(payload: CreateContenuPayload): Result<Unit> {
        return runCatching {
            val fileBytes = payload.fileBytes
            if (fileBytes == null || fileBytes.isEmpty()) {
                throw ContenuApiException("Un media (image ou video) est obligatoire pour publier.")
            }
            val mime = payload.fileMimeType
                ?.takeIf { it.isNotBlank() }
                ?: "application/octet-stream"
            val fileName = payload.fileName?.takeIf { it.isNotBlank() } ?: "media.bin"
            val response = client.submitFormWithBinaryData(
                url = "${ApiConfig.BASE_URL}/api/v1/public/contenu",
                formData = formData {
                    append("userId", payload.userId.toString())
                    append("typeContenuId", payload.typeContenuId.toString())
                    append("title", payload.title)
                    append("description", payload.description)
                    append("scope", payload.scopeId.toString())
                    append(
                        key = "fileContent",
                        value = fileBytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, mime)
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                        },
                    )
                },
            )
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw ContenuApiException(
                    extractMessage(bodyText) ?: "Publication impossible (${response.status.value})"
                )
            }
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
    private const val KEY_FEED_CACHE = "lawapp_contenu_feed_cache"
    private const val KEY_LOCAL_LIKES = "lawapp_contenu_local_likes"

    private val api = ContenuApi()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }
    private val store: LocalStore by lazy { createLocalStore() }

    private var memoryCache: List<ContenuFeedItem>? = null

    fun cachedFeed(): List<ContenuFeedItem> {
        memoryCache?.let { return it }
        val disk = loadFeedCache()
        memoryCache = disk
        return disk
    }

    suspend fun loadHomeFeed(forceRefresh: Boolean = false): Result<List<ContenuFeedItem>> {
        if (!forceRefresh) {
            val cached = cachedFeed()
            if (cached.isNotEmpty()) {
                return Result.success(cached)
            }
        }
        val currentUserId = AuthRepository.currentSession?.profile?.userId
        return api.getPublicContenus().map { items ->
            val mapped = items.mapNotNull { it.toFeedItem(currentUserId) }
                .sortedByDescending { it.createdAt.orEmpty() }
                .let(::applyLocalLikes)
            persistFeedCache(mapped)
            memoryCache = mapped
            mapped
        }
    }

    suspend fun refreshHomeFeed(): Result<List<ContenuFeedItem>> = loadHomeFeed(forceRefresh = true)

    suspend fun publishContenu(
        title: String,
        description: String,
        scopeId: Long,
        fileName: String? = null,
        fileMimeType: String? = null,
        fileUri: String? = null,
    ): Result<Unit> {
        val userId = AuthRepository.currentSession?.profile?.userId
            ?: return Result.failure(ContenuApiException("Connectez-vous pour publier un contenu."))
        val typeContenuId = resolveTypeContenuId(fileMimeType, fileName)
        if (fileUri.isNullOrBlank()) {
            return Result.failure(
                ContenuApiException("Ajoutez une image ou une video avant de publier.")
            )
        }
        val fileBytes = readUriBytes(fileUri)
        if (fileBytes == null || fileBytes.isEmpty()) {
            return Result.failure(ContenuApiException("Impossible de lire le fichier sélectionné."))
        }
        return api.createContenu(
            CreateContenuPayload(
                userId = userId,
                typeContenuId = typeContenuId,
                title = title.trim(),
                description = description.trim(),
                scopeId = scopeId,
                fileName = fileName,
                fileMimeType = fileMimeType,
                fileBytes = fileBytes,
            )
        ).onSuccess {
            memoryCache = null
            refreshHomeFeed()
        }
    }

    /**
     * Toggle like locally (and sync like to API when turning on).
     * Unlike is kept locally attached to the contenu.
     */
    suspend fun toggleLike(contenuId: Long): Result<List<ContenuFeedItem>> {
        val current = cachedFeed().toMutableList()
        val index = current.indexOfFirst { it.id == contenuId }
        if (index < 0) {
            return Result.failure(ContenuApiException("Contenu introuvable"))
        }
        val item = current[index]
        val nextLiked = !item.likedByMe
        val nextCount = if (nextLiked) {
            item.likeCount + 1
        } else {
            (item.likeCount - 1).coerceAtLeast(0)
        }
        current[index] = item.copy(likedByMe = nextLiked, likeCount = nextCount)
        persistLocalLike(contenuId, nextLiked)
        memoryCache = current
        persistFeedCache(current)

        if (nextLiked) {
            val session = AuthRepository.currentSession
            val userId = session?.profile?.userId
            val token = session?.accessToken
            if (session != null && userId != null && !token.isNullOrBlank()) {
                api.likeContenu(contenuId, userId, token).onFailure {
                    // Keep local like anyway; UI already updated.
                }
            }
        }
        return Result.success(current)
    }

    private fun applyLocalLikes(items: List<ContenuFeedItem>): List<ContenuFeedItem> {
        val local = loadLocalLikes()
        if (local.isEmpty()) return items
        return items.map { item ->
            val liked = local[item.id] ?: return@map item
            val wasLiked = item.likedByMe
            val count = when {
                !wasLiked && liked -> item.likeCount + 1
                wasLiked && !liked -> (item.likeCount - 1).coerceAtLeast(0)
                else -> item.likeCount
            }
            item.copy(likedByMe = liked, likeCount = count)
        }
    }

    private fun persistLocalLike(contenuId: Long, liked: Boolean) {
        val map = loadLocalLikes().toMutableMap()
        map[contenuId] = liked
        val payload = LocalLikesStore(likes = map.map { LocalLikeEntry(it.key, it.value) })
        store.putString(KEY_LOCAL_LIKES, json.encodeToString(LocalLikesStore.serializer(), payload))
    }

    private fun loadLocalLikes(): Map<Long, Boolean> {
        val raw = store.getString(KEY_LOCAL_LIKES) ?: return emptyMap()
        return runCatching {
            json.decodeFromString(LocalLikesStore.serializer(), raw)
                .likes
                .associate { it.contenuId to it.liked }
        }.getOrDefault(emptyMap())
    }

    private fun persistFeedCache(items: List<ContenuFeedItem>) {
        store.putString(
            KEY_FEED_CACHE,
            json.encodeToString(ContenuFeedCache.serializer(), ContenuFeedCache(items))
        )
    }

    private fun loadFeedCache(): List<ContenuFeedItem> {
        val raw = store.getString(KEY_FEED_CACHE) ?: return emptyList()
        return runCatching {
            json.decodeFromString(ContenuFeedCache.serializer(), raw).items
                .sortedByDescending { it.createdAt.orEmpty() }
                .let(::applyLocalLikes)
        }.getOrDefault(emptyList())
    }
}
