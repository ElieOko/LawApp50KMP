package emy.partners.lawapp.data.remote.quiz

import emy.partners.lawapp.data.local.LocalStore
import emy.partners.lawapp.data.local.createLocalStore
import emy.partners.lawapp.data.remote.ApiConfig
import emy.partners.lawapp.data.remote.AppJson
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.data.remote.createHttpClient
import emy.partners.lawapp.data.remote.extractArray
import emy.partners.lawapp.data.remote.extractMessage
import emy.partners.lawapp.data.remote.extractObject
import emy.partners.lawapp.data.remote.withBearerToken
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class QuizApi(
    private val client: HttpClient = createHttpClient(),
    private val json: Json = AppJson,
) {
    suspend fun listQuizzes(accessToken: String): Result<List<QuizSummaryDto>> = runCatching {
        val body = authorizedGet(accessToken, "/v1/protected/quiz", "/api/v1/protected/quiz")
        json.extractArray(body, "quiz", "quizzes", "data", "content", "items")
            .mapNotNull { element ->
                runCatching { json.decodeFromJsonElement(QuizSummaryDto.serializer(), element) }.getOrNull()
            }
    }

    suspend fun getQuizDetail(accessToken: String, quizId: Long): Result<QuizDetailDto> = runCatching {
        val body = authorizedGet(
            accessToken,
            "/v1/private/quiz/$quizId",
            "/api/v1/private/quiz/$quizId",
            "/v1/protected/quiz/$quizId",
        )
        val obj = json.extractObject(body, "quiz", "data", "content")
            ?: throw QuizApiException("Reponse quiz invalide")
        json.decodeFromJsonElement(QuizDetailDto.serializer(), obj)
    }

    suspend fun getProgression(accessToken: String): Result<QuizUserProgressOverviewDto> = runCatching {
        val body = authorizedGet(
            accessToken,
            "/v1/protected/quiz/progression",
            "/api/v1/protected/quiz/progression",
        )
        runCatching {
            json.decodeFromString(QuizUserProgressOverviewDto.serializer(), body)
        }.getOrElse {
            val obj = json.extractObject(body, "data", "progression", "overview")
                ?: throw QuizApiException("Reponse progression invalide")
            json.decodeFromJsonElement(QuizUserProgressOverviewDto.serializer(), obj)
        }
    }

    suspend fun listNotionTypes(accessToken: String): Result<List<QuizNotionTypeDto>> = runCatching {
        val body = authorizedGet(
            accessToken,
            "/v1/public/quiz/notion-types",
            "/api/v1/public/quiz/notion-types",
            "/v1/protected/quiz/notion-types",
            "/api/v1/protected/quiz/notion-types",
        )
        json.extractArray(body, "notionTypes", "types", "data", "content", "items")
            .mapNotNull { element ->
                runCatching { json.decodeFromJsonElement(QuizNotionTypeDto.serializer(), element) }.getOrNull()
            }
    }

    suspend fun createNotionType(
        accessToken: String,
        request: QuizNotionTypeRequest,
    ): Result<QuizNotionTypeDto> = runCatching {
        val paths = listOf("/v1/private/quiz/notion-types", "/api/v1/private/quiz/notion-types")
        var lastError: String? = null
        for (path in paths) {
            val response = client.post {
                url.takeFrom("${ApiConfig.BASE_URL}$path")
                withBearerToken(accessToken)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                return@runCatching json.extractObject(body, "notionType", "data", "content")?.let {
                    runCatching { json.decodeFromJsonElement(QuizNotionTypeDto.serializer(), it) }.getOrNull()
                } ?: QuizNotionTypeDto(code = request.code, label = request.label, description = request.description)
            }
            lastError = json.extractMessage(body) ?: "Creation du type de notion impossible (${response.status.value})"
            if (response.status.value != 404) throw QuizApiException(lastError)
        }
        throw QuizApiException(lastError ?: "Creation du type de notion impossible")
    }

    suspend fun createQuiz(accessToken: String, request: QuizCreateRequest): Result<QuizSummaryDto> = runCatching {
        val paths = listOf("/v1/private/quiz", "/api/v1/private/quiz")
        var lastError: String? = null
        for (path in paths) {
            val response = client.post {
                url.takeFrom("${ApiConfig.BASE_URL}$path")
                withBearerToken(accessToken)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.bodyAsText()
            if (response.status.isSuccess()) {
                val obj = json.extractObject(body, "quiz", "data", "content")
                    ?: throw QuizApiException("Quiz cree mais reponse illisible")
                return@runCatching json.decodeFromJsonElement(QuizSummaryDto.serializer(), obj)
            }
            lastError = json.extractMessage(body) ?: "Creation du quiz impossible (${response.status.value})"
            if (response.status.value != 404) throw QuizApiException(lastError)
        }
        throw QuizApiException(lastError ?: "Creation du quiz impossible")
    }

    suspend fun recordLevelProgress(
        accessToken: String,
        quizId: Long,
        levelOrder: Int,
        score: Double,
    ): Result<Unit> = runCatching {
        val paths = listOf(
            "/v1/protected/quiz/$quizId/progression",
            "/api/v1/protected/quiz/$quizId/progression",
        )
        var lastError: String? = null
        for (path in paths) {
            val response = client.post {
                url.takeFrom("${ApiConfig.BASE_URL}$path")
                withBearerToken(accessToken)
                contentType(ContentType.Application.Json)
                setBody(QuizProgressUpdateRequest(levelOrder = levelOrder, score = score))
            }
            val body = response.bodyAsText()
            if (response.status.isSuccess()) return@runCatching
            lastError = json.extractMessage(body) ?: "Progression impossible (${response.status.value})"
            if (response.status.value != 404) {
                throw QuizApiException(lastError)
            }
        }
        throw QuizApiException(lastError ?: "Progression impossible")
    }

    private suspend fun authorizedGet(accessToken: String, vararg paths: String): String {
        var lastError: String? = null
        for (path in paths) {
            val response = client.get {
                url.takeFrom("${ApiConfig.BASE_URL}$path")
                withBearerToken(accessToken)
            }
            val body = response.bodyAsText()
            if (response.status.isSuccess()) return body
            lastError = json.extractMessage(body) ?: "Chargement impossible (${response.status.value})"
            if (response.status.value != 404) {
                throw QuizApiException(lastError)
            }
        }
        throw QuizApiException(lastError ?: "Chargement impossible")
    }
}

class QuizApiException(message: String) : Exception(message)

object QuizRepository {
    private const val KEY_QUIZ_CACHE = "lawapp_quiz_list_cache"

    private val api = QuizApi()
    private val json = AppJson
    private val store: LocalStore by lazy { createLocalStore() }

    private var memoryCache: List<QuizSummary>? = null

    fun cachedQuizzes(): List<QuizSummary> {
        memoryCache?.let { return it }
        val disk = loadCache()
        memoryCache = disk
        return disk
    }

    suspend fun loadQuizzes(forceRefresh: Boolean = false): Result<List<QuizSummary>> {
        if (!forceRefresh) {
            val cached = cachedQuizzes()
            if (cached.isNotEmpty()) return Result.success(cached)
        }
        val token = requireToken()
            ?: return Result.failure(QuizApiException("Connectez-vous pour charger les quiz."))
        val listResult = api.listQuizzes(token)
        if (listResult.isFailure) {
            return Result.failure(listResult.exceptionOrNull() ?: QuizApiException("Chargement des quiz impossible"))
        }
        val progress = api.getProgression(token).getOrNull()?.progressByQuizId().orEmpty()
        val mapped = listResult.getOrDefault(emptyList())
            .mapNotNull { dto -> dto.toSummary(progress[dto.id]) }
        persistCache(mapped)
        memoryCache = mapped
        return Result.success(mapped)
    }

    suspend fun refreshQuizzes(): Result<List<QuizSummary>> = loadQuizzes(forceRefresh = true)

    suspend fun getQuizDetail(quizId: Long): Result<QuizPlayContent> {
        val token = requireToken()
            ?: return Result.failure(QuizApiException("Connectez-vous pour ouvrir un quiz."))
        return api.getQuizDetail(token, quizId).mapCatching { dto ->
            dto.toPlayContent() ?: throw QuizApiException("Quiz sans questions")
        }
    }

    suspend fun loadNotionTypes(): Result<List<QuizNotionTypeDto>> {
        val token = requireToken()
            ?: return Result.failure(QuizApiException("Connectez-vous pour charger les types de notion."))
        return api.listNotionTypes(token)
    }

    suspend fun createQuiz(
        title: String,
        description: String?,
        notionTypeId: Long?,
        notionLabel: String?,
        levels: List<QuizLevelRequestDto>,
    ): Result<QuizSummary> {
        val token = requireToken()
            ?: return Result.failure(QuizApiException("Connectez-vous pour creer un quiz."))
        if (title.isBlank()) {
            return Result.failure(QuizApiException("Le titre du quiz est obligatoire."))
        }
        if (levels.isEmpty() || levels.all { it.questions.isEmpty() }) {
            return Result.failure(QuizApiException("Ajoutez au moins une question."))
        }
        val resolvedTypeId = notionTypeId ?: run {
            val code = (notionLabel ?: title).trim().take(12).uppercase().replace(" ", "_")
            val created = api.createNotionType(
                token,
                QuizNotionTypeRequest(
                    code = code.ifBlank { "DROIT" },
                    label = notionLabel?.takeIf { it.isNotBlank() } ?: "Droit",
                ),
            ).getOrElse { return Result.failure(it) }
            created.id ?: return Result.failure(QuizApiException("Type de notion introuvable."))
        }
        return api.createQuiz(
            token,
            QuizCreateRequest(
                title = title.trim(),
                description = description?.trim()?.takeIf { it.isNotBlank() },
                notionTypeId = resolvedTypeId,
                levels = levels,
            ),
        ).mapCatching { dto ->
            dto.toSummary() ?: QuizSummary(
                id = dto.id ?: 0L,
                title = title.trim(),
                description = description.orEmpty(),
                notionTypeId = resolvedTypeId,
                notionTypeLabel = notionLabel ?: "Quiz",
                totalLevels = levels.size,
            )
        }.onSuccess { created ->
            if (created.id > 0L) {
                memoryCache = listOf(created) + cachedQuizzes().filter { it.id != created.id }
                persistCache(cachedQuizzes())
            }
        }
    }

    fun history(): List<QuizSummary> = cachedQuizzes().filter { it.highestCompletedLevelOrder > 0 || it.completed }

    suspend fun recordLevelProgress(quizId: Long, levelOrder: Int, score: Double): Result<Unit> {
        val token = requireToken()
            ?: return Result.failure(QuizApiException("Connectez-vous pour enregistrer la progression."))
        return api.recordLevelProgress(token, quizId, levelOrder, score).onSuccess {
            val current = cachedQuizzes().toMutableList()
            val index = current.indexOfFirst { it.id == quizId }
            if (index >= 0) {
                val item = current[index]
                val nextCompleted = (item.highestCompletedLevelOrder).coerceAtLeast(levelOrder)
                val total = item.totalLevels.coerceAtLeast(nextCompleted)
                current[index] = item.copy(
                    highestCompletedLevelOrder = nextCompleted,
                    totalLevels = total,
                    progressPercent = if (total > 0) (nextCompleted.toFloat() / total).coerceIn(0f, 1f) else item.progressPercent,
                    completed = total > 0 && nextCompleted >= total,
                    score = score,
                )
                memoryCache = current
                persistCache(current)
            }
        }
    }

    private fun requireToken(): String? =
        AuthRepository.currentSession?.accessToken?.takeIf { it.isNotBlank() }

    private fun persistCache(items: List<QuizSummary>) {
        store.putString(
            KEY_QUIZ_CACHE,
            json.encodeToString(QuizCache.serializer(), QuizCache(items = items.map { it.toCacheItem() })),
        )
    }

    private fun loadCache(): List<QuizSummary> {
        val raw = store.getString(KEY_QUIZ_CACHE) ?: return emptyList()
        return runCatching {
            json.decodeFromString(QuizCache.serializer(), raw).items.map { it.toSummary() }
        }.getOrDefault(emptyList())
    }
}

@kotlinx.serialization.Serializable
private data class QuizCache(
    val items: List<QuizCacheItem> = emptyList(),
)

@kotlinx.serialization.Serializable
private data class QuizCacheItem(
    val id: Long,
    val title: String,
    val description: String,
    val notionTypeId: Long? = null,
    val notionTypeLabel: String,
    val totalLevels: Int = 0,
    val highestCompletedLevelOrder: Int = 0,
    val progressPercent: Float = 0f,
    val score: Double? = null,
    val completed: Boolean = false,
)

private fun QuizSummary.toCacheItem() = QuizCacheItem(
    id = id,
    title = title,
    description = description,
    notionTypeId = notionTypeId,
    notionTypeLabel = notionTypeLabel,
    totalLevels = totalLevels,
    highestCompletedLevelOrder = highestCompletedLevelOrder,
    progressPercent = progressPercent,
    score = score,
    completed = completed,
)

private fun QuizCacheItem.toSummary() = QuizSummary(
    id = id,
    title = title,
    description = description,
    notionTypeId = notionTypeId,
    notionTypeLabel = notionTypeLabel,
    totalLevels = totalLevels,
    highestCompletedLevelOrder = highestCompletedLevelOrder,
    progressPercent = progressPercent,
    score = score,
    completed = completed,
)
