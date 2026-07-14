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

    suspend fun createCommentaire(
        contenuId: Long,
        userId: Long,
        description: String,
        accessToken: String,
    ): Result<Unit> {
        return runCatching {
            val response = client.post {
                url.takeFrom("${ApiConfig.BASE_URL}/api/v1/private/commentaire")
                withBearerToken(accessToken)
                contentType(ContentType.Application.Json)
                setBody(
                    CreateCommentaireRequest(
                        contenuId = contenuId,
                        userId = userId,
                        description = description,
                    )
                )
            }
            val bodyText = response.bodyAsText()
            if (!response.status.isSuccess()) {
                throw ContenuApiException(
                    extractMessage(bodyText) ?: "Commentaire impossible (${response.status.value})"
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
    private const val KEY_LOCAL_LIKES_PREFIX = "lawapp_contenu_local_likes_u"
    private const val KEY_LOCAL_LIKES_LEGACY = "lawapp_contenu_local_likes"

    private val api = ContenuApi()
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    }
    private val store: LocalStore by lazy { createLocalStore() }

    private var memoryCache: List<ContenuFeedItem>? = null
    private var memoryLikedForUserId: Long? = null

    /** Contenu actuellement affiché sur Home — conservé entre les navigations d'onglets. */
    private var lastViewedContenuId: Long? = null

    private fun currentUserId(): Long? = AuthRepository.currentSession?.profile?.userId

    private fun likesStorageKey(userId: Long): String = KEY_LOCAL_LIKES_PREFIX + userId

    fun cachedFeed(): List<ContenuFeedItem> {
        val userId = currentUserId()
        memoryCache?.let { cached ->
            if (memoryLikedForUserId == userId) return cached
            val reconciled = reconcileLikesForCurrentUser(cached)
            memoryCache = reconciled
            memoryLikedForUserId = userId
            persistFeedCache(reconciled)
            return reconciled
        }
        val disk = loadFeedCache()
        memoryCache = disk
        memoryLikedForUserId = userId
        return disk
    }

    /** Appele apres login/logout pour recalculer likedByMe selon le profil. */
    fun onAuthUserChanged() {
        val current = memoryCache ?: loadFeedCache()
        if (current.isEmpty()) {
            memoryCache = null
            memoryLikedForUserId = currentUserId()
            return
        }
        val reconciled = reconcileLikesForCurrentUser(current)
        memoryCache = reconciled
        memoryLikedForUserId = currentUserId()
        persistFeedCache(reconciled)
    }

    fun lastViewedContenuId(): Long? = lastViewedContenuId

    fun rememberLastViewedContenuId(contenuId: Long) {
        lastViewedContenuId = contenuId
    }

    fun indexOfLastViewed(items: List<ContenuFeedItem>): Int {
        val id = lastViewedContenuId ?: return 0
        val index = items.indexOfFirst { it.id == id }
        return if (index >= 0) index else 0
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
                .let { applyLocalLikes(it, currentUserId) }
            persistFeedCache(mapped)
            memoryCache = mapped
            memoryLikedForUserId = currentUserId
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
        )
        // Pas de refresh auto : le feed Home se met a jour via pull-to-refresh.
    }

    /**
     * Toggle like for the connected profile only (local state keyed by userId).
     * Syncs like to API when turning on.
     */
    suspend fun toggleLike(contenuId: Long): Result<List<ContenuFeedItem>> {
        val session = AuthRepository.currentSession
        val userId = session?.profile?.userId
        val token = session?.accessToken
        if (session == null || userId == null || token.isNullOrBlank()) {
            return Result.failure(ContenuApiException("Connectez-vous pour liker un contenu."))
        }

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
        persistLocalLike(userId, contenuId, nextLiked)
        memoryCache = current
        memoryLikedForUserId = userId
        persistFeedCache(current)

        if (nextLiked) {
            api.likeContenu(contenuId, userId, token).onFailure {
                // Keep local like for this profile; UI already updated.
            }
        }
        return Result.success(current)
    }

    suspend fun addComment(contenuId: Long, description: String): Result<List<ContenuFeedItem>> {
        val text = description.trim()
        if (text.isBlank()) {
            return Result.failure(ContenuApiException("Ecrivez un commentaire avant d'envoyer."))
        }
        val session = AuthRepository.currentSession
        val userId = session?.profile?.userId
        val token = session?.accessToken
        if (session == null || userId == null || token.isNullOrBlank()) {
            return Result.failure(ContenuApiException("Connectez-vous pour commenter."))
        }

        val apiResult = api.createCommentaire(
            contenuId = contenuId,
            userId = userId,
            description = text,
            accessToken = token,
        )
        if (apiResult.isFailure) {
            return Result.failure(
                apiResult.exceptionOrNull() ?: ContenuApiException("Commentaire impossible")
            )
        }

        val authorName = session.profile?.displayName ?: "Moi"

        val current = cachedFeed().toMutableList()
        val index = current.indexOfFirst { it.id == contenuId }
        if (index >= 0) {
            val item = current[index]
            val localComment = ContenuCommentUi(
                id = -kotlin.random.Random.nextLong(1, Long.MAX_VALUE),
                text = text,
                authorName = authorName,
            )
            current[index] = item.copy(
                comments = item.comments + localComment,
                commentCount = item.commentCount + 1,
            )
            memoryCache = current
            persistFeedCache(current)
            return Result.success(current)
        }

        // Contenu absent du cache : rafraichir le feed.
        return refreshHomeFeed()
    }

    private fun reconcileLikesForCurrentUser(items: List<ContenuFeedItem>): List<ContenuFeedItem> {
        val userId = currentUserId()
        // Autre profil / deconnecte : ne pas reutiliser le likedByMe precedent.
        val base = items.map { item ->
            if (item.likedByMe) item.copy(likedByMe = false) else item
        }
        return applyLocalLikes(base, userId)
    }

    private fun applyLocalLikes(
        items: List<ContenuFeedItem>,
        userId: Long? = currentUserId(),
    ): List<ContenuFeedItem> {
        if (userId == null) {
            return items.map { if (it.likedByMe) it.copy(likedByMe = false) else it }
        }
        val local = loadLocalLikes(userId)
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

    private fun persistLocalLike(userId: Long, contenuId: Long, liked: Boolean) {
        val map = loadLocalLikes(userId).toMutableMap()
        map[contenuId] = liked
        val payload = LocalLikesStore(
            userId = userId,
            likes = map.map { LocalLikeEntry(it.key, it.value) },
        )
        store.putString(
            likesStorageKey(userId),
            json.encodeToString(LocalLikesStore.serializer(), payload),
        )
    }

    private fun loadLocalLikes(userId: Long): Map<Long, Boolean> {
        val raw = store.getString(likesStorageKey(userId))
        if (raw != null) {
            return runCatching {
                json.decodeFromString(LocalLikesStore.serializer(), raw)
                    .likes
                    .associate { it.contenuId to it.liked }
            }.getOrDefault(emptyMap())
        }
        // Migration one-shot depuis l'ancien store global.
        val legacy = store.getString(KEY_LOCAL_LIKES_LEGACY) ?: return emptyMap()
        return runCatching {
            val parsed = json.decodeFromString(LocalLikesStore.serializer(), legacy)
            val likes = parsed.likes.associate { it.contenuId to it.liked }
            if (likes.isNotEmpty()) {
                persistLocalLikeEntries(userId, likes)
                store.remove(KEY_LOCAL_LIKES_LEGACY)
            }
            likes
        }.getOrDefault(emptyMap())
    }

    private fun persistLocalLikeEntries(userId: Long, likes: Map<Long, Boolean>) {
        val payload = LocalLikesStore(
            userId = userId,
            likes = likes.map { LocalLikeEntry(it.key, it.value) },
        )
        store.putString(
            likesStorageKey(userId),
            json.encodeToString(LocalLikesStore.serializer(), payload),
        )
    }

    private fun persistFeedCache(items: List<ContenuFeedItem>) {
        store.putString(
            KEY_FEED_CACHE,
            json.encodeToString(
                ContenuFeedCache.serializer(),
                ContenuFeedCache(
                    likedForUserId = currentUserId(),
                    items = items,
                ),
            ),
        )
    }

    private fun loadFeedCache(): List<ContenuFeedItem> {
        val raw = store.getString(KEY_FEED_CACHE) ?: return emptyList()
        return runCatching {
            val cache = json.decodeFromString(ContenuFeedCache.serializer(), raw)
            val userId = currentUserId()
            val sorted = cache.items.sortedByDescending { it.createdAt.orEmpty() }
            if (cache.likedForUserId != userId) {
                reconcileLikesForCurrentUser(sorted)
            } else {
                applyLocalLikes(sorted, userId)
            }
        }.getOrDefault(emptyList())
    }
}
