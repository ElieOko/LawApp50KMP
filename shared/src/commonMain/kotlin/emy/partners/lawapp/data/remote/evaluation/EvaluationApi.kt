package emy.partners.lawapp.data.remote.evaluation

import emy.partners.lawapp.data.local.LocalStore
import emy.partners.lawapp.data.local.createLocalStore
import emy.partners.lawapp.data.remote.ApiConfig
import emy.partners.lawapp.data.remote.AppJson
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.data.remote.createHttpClient
import emy.partners.lawapp.data.remote.extractArray
import emy.partners.lawapp.data.remote.extractMessage
import emy.partners.lawapp.data.remote.extractObject
import emy.partners.lawapp.data.remote.toApiDate
import emy.partners.lawapp.data.remote.withBearerToken
import emy.partners.lawapp.domain.models.EvaluationDAO
import emy.partners.lawapp.domain.models.EvaluationSession
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class EvaluationApi(
    private val client: HttpClient = createHttpClient(),
    private val json: Json = AppJson,
) {
    suspend fun listPublicEvaluations(): Result<List<EvaluationSessionDto>> = runCatching {
        val response = client.get("/api/v1/public/evaluations")
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw EvaluationApiException(
                json.extractMessage(body) ?: "Chargement des evaluations impossible"
            )
        }
        decodeSessionList(body)
    }

    suspend fun getPublicEvaluation(id: Long): Result<EvaluationSessionDto> = runCatching {
        val response = client.get("/api/v1/public/evaluations/$id")
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw EvaluationApiException(
                json.extractMessage(body) ?: "Evaluation introuvable"
            )
        }
        decodeSessionList(body).firstOrNull()
            ?: throw EvaluationApiException("Evaluation introuvable")
    }

    suspend fun listAnswerable(accessToken: String): Result<List<EvaluationAnswerableSummaryDto>> = runCatching {
        val response = client.get {
            url.takeFrom("${ApiConfig.BASE_URL}/api/v1/protected/evaluations/repondre")
            withBearerToken(accessToken)
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw EvaluationApiException(
                json.extractMessage(body) ?: "Chargement des evaluations ouvertes impossible"
            )
        }
        json.extractArray(body, "evaluations", "session", "data", "content", "items")
            .mapNotNull { element ->
                runCatching {
                    json.decodeFromJsonElement(EvaluationAnswerableSummaryDto.serializer(), element)
                }.getOrNull()
            }
    }

    suspend fun getPassage(accessToken: String, id: Long): Result<EvaluationPassageDto> = runCatching {
        val response = client.get {
            url.takeFrom("${ApiConfig.BASE_URL}/api/v1/protected/evaluations/$id/passage")
            withBearerToken(accessToken)
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw EvaluationApiException(
                json.extractMessage(body) ?: "Feuille d'evaluation indisponible"
            )
        }
        val obj = json.extractObject(body, "passage", "evaluation", "data", "session", "content")
            ?: throw EvaluationApiException("Reponse passage invalide")
        json.decodeFromJsonElement(EvaluationPassageDto.serializer(), obj)
    }

    suspend fun submitAnswers(
        accessToken: String,
        id: Long,
        answers: List<EvaluationAnswerInput>,
    ): Result<EvaluationSubmitResult> = runCatching {
        val response = client.post {
            url.takeFrom("${ApiConfig.BASE_URL}/api/v1/protected/evaluations/$id/reponses")
            withBearerToken(accessToken)
            contentType(ContentType.Application.Json)
            setBody(
                EvaluationAnswerSubmitRequest(
                    answers = answers.map {
                        QuestionAnswerItemRequest(
                            questionId = it.questionId,
                            selectedOptionId = it.selectedOptionId,
                            textResponse = it.textResponse?.takeIf { text -> text.isNotBlank() },
                        )
                    }
                )
            )
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw EvaluationApiException(
                json.extractMessage(body) ?: "Soumission impossible (${response.status.value})"
            )
        }
        EvaluationSubmitResult(
            message = json.extractMessage(body) ?: "Evaluation soumise",
            rawResponse = body,
        )
    }

    suspend fun createEvaluation(
        accessToken: String,
        request: EvaluationCreateRequest,
    ): Result<EvaluationSessionDto> = runCatching {
        val response = client.post {
            url.takeFrom("${ApiConfig.BASE_URL}/api/v1/private/evaluations")
            withBearerToken(accessToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw EvaluationApiException(
                json.extractMessage(body) ?: "Creation impossible (${response.status.value})"
            )
        }
        decodeSessionList(body).firstOrNull()
            ?: json.extractObject(body, "session", "evaluation", "data")?.let { obj ->
                runCatching { json.decodeFromJsonElement(EvaluationSessionDto.serializer(), obj) }.getOrNull()
            }
            ?: throw EvaluationApiException("Evaluation creee mais reponse illisible")
    }

    private fun decodeSessionList(body: String): List<EvaluationSessionDto> =
        json.extractArray(body, "session", "evaluations", "data", "content", "items")
            .mapNotNull { element ->
                runCatching {
                    json.decodeFromJsonElement(EvaluationSessionDto.serializer(), element)
                }.getOrNull()
            }
}

class EvaluationApiException(message: String) : Exception(message)

object EvaluationRepository {
    private const val KEY_EVAL_CACHE = "lawapp_evaluation_list_cache"
    private const val KEY_SUBMITTED_PREFIX = "lawapp_evaluation_submitted_u"
    private const val KEY_ACTIVE_ATTEMPT_PREFIX = "lawapp_evaluation_active_u"
    private const val KEY_ATTEMPT_PREFIX = "lawapp_evaluation_attempt_u"

    private val api = EvaluationApi()
    private val json = AppJson
    private val store: LocalStore by lazy { createLocalStore() }

    private var memorySessions: List<EvaluationSession>? = null
    private var memoryDetails: Map<Long, EvaluationSessionDto> = emptyMap()

    fun cachedSessions(): List<EvaluationSession> {
        memorySessions?.let { return it }
        val disk = loadCache()
        memorySessions = disk
        return disk
    }

    fun cachedSession(id: Long): EvaluationSession? =
        cachedSessions().firstOrNull { it.id == id }

    fun cachedDetail(id: Long): EvaluationSessionDto? = memoryDetails[id]

    suspend fun loadEvaluations(forceRefresh: Boolean = false): Result<List<EvaluationSession>> {
        if (!forceRefresh) {
            val cached = cachedSessions()
            if (cached.isNotEmpty()) return Result.success(cached)
        }
        val publicResult = api.listPublicEvaluations()
        if (publicResult.isFailure) {
            return Result.failure(
                publicResult.exceptionOrNull() ?: EvaluationApiException("Chargement des evaluations impossible")
            )
        }
        val details = publicResult.getOrDefault(emptyList()).mapNotNull { dto ->
            dto.id?.let { it to dto }
        }.toMap()
        memoryDetails = details

        val token = AuthRepository.currentSession?.accessToken?.takeIf { it.isNotBlank() }
        val answerableIds = if (token != null) {
            api.listAnswerable(token).getOrNull()
                ?.mapNotNull { it.id }
                ?.toSet()
                .orEmpty()
        } else {
            emptySet()
        }
        val submitted = loadSubmittedIds()
        val mapped = details.values.mapNotNull { dto ->
            val id = dto.id ?: return@mapNotNull null
            val canAnswer = if (token == null) true else answerableIds.contains(id)
            val session = dto.toSession(
                canAnswer = canAnswer && !submitted.contains(id),
                alreadySubmitted = submitted.contains(id),
            ) ?: return@mapNotNull null
            applyAttemptProgress(session)
        }.sortedByDescending { it.id }
        memorySessions = mapped
        persistCache(mapped)
        return Result.success(mapped)
    }

    suspend fun refreshEvaluations(): Result<List<EvaluationSession>> =
        loadEvaluations(forceRefresh = true)

    suspend fun getEvaluation(id: Long): Result<EvaluationSession> {
        cachedSession(id)?.let { cached ->
            if (memoryDetails.containsKey(id)) return Result.success(cached)
        }
        return api.getPublicEvaluation(id).mapCatching { dto ->
            dto.id?.let { memoryDetails = memoryDetails + (it to dto) }
            val submitted = loadSubmittedIds()
            dto.toSession(
                canAnswer = !submitted.contains(dto.id),
                alreadySubmitted = submitted.contains(dto.id),
            )?.let(::applyAttemptProgress) ?: throw EvaluationApiException("Evaluation invalide")
        }.onSuccess { session ->
            val current = cachedSessions().toMutableList()
            val index = current.indexOfFirst { it.id == session.id }
            if (index >= 0) current[index] = session else current.add(0, session)
            memorySessions = current
            persistCache(current)
        }
    }

    suspend fun getTakeSheet(id: Long): Result<EvaluationTakeSheet> {
        val token = AuthRepository.currentSession?.accessToken?.takeIf { it.isNotBlank() }
        if (token != null) {
            val passage = api.getPassage(token, id)
            if (passage.isSuccess) {
                val sheet = passage.getOrNull()?.toTakeSheet()
                    ?.withFallbackMinutes(fallbackCompteurMinutes(id))
                if (sheet != null && sheet.questions.isNotEmpty()) {
                    return Result.success(sheet)
                }
            }
        }
        val detail = memoryDetails[id] ?: api.getPublicEvaluation(id).getOrElse { error ->
            return Result.failure(error)
        }
        detail.id?.let { memoryDetails = memoryDetails + (it to detail) }
        val sheet = detail.toTakeSheet()
            ?.withFallbackMinutes(fallbackCompteurMinutes(id))
            ?: return Result.failure(EvaluationApiException("Aucune question pour cette evaluation"))
        if (sheet.questions.isEmpty()) {
            return Result.failure(EvaluationApiException("Aucune question pour cette evaluation"))
        }
        return Result.success(sheet)
    }

    suspend fun submitAnswers(
        id: Long,
        answers: List<EvaluationAnswerInput>,
    ): Result<EvaluationSubmitResult> {
        val token = AuthRepository.currentSession?.accessToken?.takeIf { it.isNotBlank() }
            ?: return Result.failure(EvaluationApiException("Connectez-vous pour soumettre l'evaluation."))
        if (answers.isEmpty()) {
            return Result.failure(EvaluationApiException("Ajoutez au moins une reponse avant de soumettre."))
        }
        return api.submitAnswers(token, id, answers).onSuccess {
            markSubmitted(id)
            clearAttempt(id)
            val current = cachedSessions().toMutableList()
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) {
                val item = current[index]
                current[index] = item.copy(
                    status = emy.partners.lawapp.domain.models.EvaluationStatus.Completed,
                    progress = 1f,
                    completedQuestions = item.questionCount,
                    canAnswer = false,
                    alreadySubmitted = true,
                    domain = "Terminee",
                )
                memorySessions = current
                persistCache(current)
            }
        }
    }

    suspend fun createEvaluation(dao: EvaluationDAO): Result<EvaluationSession> {
        val token = AuthRepository.currentSession?.accessToken?.takeIf { it.isNotBlank() }
            ?: return Result.failure(EvaluationApiException("Connectez-vous pour creer une evaluation."))
        val request = dao.toCreateRequest(
            startDateApi = toApiDate(dao.startDate),
            endDateApi = toApiDate(dao.endDate),
        )
        return api.createEvaluation(token, request).mapCatching { dto ->
            dto.id?.let { memoryDetails = memoryDetails + (it to dto) }
            dto.toSession(canAnswer = false, alreadySubmitted = false)
                ?: dao.toLocalSession(cachedSessions().size)
        }.onSuccess { session ->
            val current = cachedSessions().toMutableList()
            if (current.none { it.id == session.id }) {
                current.add(0, session)
            }
            memorySessions = current
            persistCache(current)
        }
    }

    fun activeAttemptEvaluationId(): Long? {
        val userId = AuthRepository.currentSession?.profile?.userId ?: return null
        val raw = store.getString(activeAttemptKey(userId)) ?: return null
        val id = raw.toLongOrNull() ?: return null
        if (loadSubmittedIds().contains(id)) {
            clearAttempt(id)
            return null
        }
        return id
    }

    fun loadAttemptProgress(evaluationId: Long): EvaluationAttemptProgress? {
        val userId = AuthRepository.currentSession?.profile?.userId ?: return null
        val raw = store.getString(attemptKey(userId, evaluationId)) ?: return null
        return runCatching {
            json.decodeFromString(EvaluationAttemptProgress.serializer(), raw)
        }.getOrNull()
    }

    fun beginAttempt(evaluationId: Long, questionCount: Int, durationMinutes: Long = 0) {
        val userId = AuthRepository.currentSession?.profile?.userId ?: return
        store.putString(activeAttemptKey(userId), evaluationId.toString())
        val existing = loadAttemptProgress(evaluationId)
        val startedAt = existing?.startedAtEpochMs?.takeIf { it > 0L } ?: nowEpochMs()
        val duration = resolveCompteurMinutes(existing?.durationMinutes, durationMinutes)
        if (existing == null) {
            saveAttemptProgress(
                EvaluationAttemptProgress(
                    evaluationId = evaluationId,
                    questionCount = questionCount,
                    durationMinutes = duration,
                    startedAtEpochMs = startedAt,
                )
            )
            return
        }
        val needsUpdate = existing.startedAtEpochMs == null ||
            existing.startedAtEpochMs <= 0L ||
            (existing.durationMinutes <= 0L && duration > 0L) ||
            (existing.questionCount <= 0 && questionCount > 0)
        if (needsUpdate) {
            saveAttemptProgress(
                existing.copy(
                    questionCount = existing.questionCount.takeIf { it > 0 } ?: questionCount,
                    durationMinutes = duration,
                    startedAtEpochMs = startedAt,
                )
            )
        }
    }

    fun saveAttemptProgress(progress: EvaluationAttemptProgress) {
        val userId = AuthRepository.currentSession?.profile?.userId ?: return
        store.putString(activeAttemptKey(userId), progress.evaluationId.toString())
        store.putString(
            attemptKey(userId, progress.evaluationId),
            json.encodeToString(EvaluationAttemptProgress.serializer(), progress),
        )
        applyAttemptToCachedSession(progress)
    }

    fun clearAttempt(evaluationId: Long) {
        val userId = AuthRepository.currentSession?.profile?.userId ?: return
        store.remove(attemptKey(userId, evaluationId))
        val active = store.getString(activeAttemptKey(userId))?.toLongOrNull()
        if (active == null || active == evaluationId) {
            store.remove(activeAttemptKey(userId))
        }
    }

    private fun applyAttemptProgress(session: EvaluationSession): EvaluationSession {
        if (session.alreadySubmitted) return session
        val attempt = loadAttemptProgress(session.id) ?: return session
        val total = session.questionCount.coerceAtLeast(attempt.questionCount)
        val answered = attempt.answeredCount.coerceAtMost(total)
        if (answered <= 0) return session
        return session.copy(
            status = emy.partners.lawapp.domain.models.EvaluationStatus.InProgress,
            progress = if (total > 0) answered.toFloat() / total else session.progress,
            completedQuestions = answered,
        )
    }

    private fun applyAttemptToCachedSession(progress: EvaluationAttemptProgress) {
        val current = cachedSessions().toMutableList()
        val index = current.indexOfFirst { it.id == progress.evaluationId }
        if (index < 0) return
        current[index] = applyAttemptProgress(current[index])
        memorySessions = current
        persistCache(current)
    }

    private fun activeAttemptKey(userId: Long): String = KEY_ACTIVE_ATTEMPT_PREFIX + userId

    private fun attemptKey(userId: Long, evaluationId: Long): String =
        KEY_ATTEMPT_PREFIX + userId + "_e" + evaluationId

    private fun markSubmitted(id: Long) {
        val userId = AuthRepository.currentSession?.profile?.userId ?: return
        val ids = loadSubmittedIds().toMutableSet()
        ids.add(id)
        store.putString(
            submittedKey(userId),
            json.encodeToString(SubmittedStore.serializer(), SubmittedStore(ids = ids.toList())),
        )
    }

    private fun loadSubmittedIds(): Set<Long> {
        val userId = AuthRepository.currentSession?.profile?.userId ?: return emptySet()
        val raw = store.getString(submittedKey(userId)) ?: return emptySet()
        return runCatching {
            json.decodeFromString(SubmittedStore.serializer(), raw).ids.toSet()
        }.getOrDefault(emptySet())
    }

    private fun submittedKey(userId: Long): String = KEY_SUBMITTED_PREFIX + userId

    private fun fallbackCompteurMinutes(id: Long): Long =
        resolveCompteurMinutes(
            memoryDetails[id]?.compteur,
            cachedSession(id)?.compteurMinutes,
        )

    private fun persistCache(items: List<EvaluationSession>) {
        store.putString(
            KEY_EVAL_CACHE,
            json.encodeToString(EvaluationCache.serializer(), EvaluationCache(items = items.map { it.toCacheItem() })),
        )
    }

    private fun loadCache(): List<EvaluationSession> {
        val raw = store.getString(KEY_EVAL_CACHE) ?: return emptyList()
        return runCatching {
            json.decodeFromString(EvaluationCache.serializer(), raw).items.map { it.toSession() }
        }.getOrDefault(emptyList())
    }
}

@Serializable
private data class SubmittedStore(
    val ids: List<Long> = emptyList(),
)

@Serializable
private data class EvaluationCache(
    val items: List<EvaluationCacheItem> = emptyList(),
)

@Serializable
private data class EvaluationCacheItem(
    val id: Long,
    val title: String,
    val domain: String,
    val description: String,
    val completed: Boolean,
    val progress: Float,
    val score: Int? = null,
    val questionCount: Int,
    val completedQuestions: Int,
    val duration: String,
    val updatedAt: String,
    val level: String,
    val canAnswer: Boolean = true,
    val alreadySubmitted: Boolean = false,
    val startDate: String? = null,
    val endDate: String? = null,
    val compteurMinutes: Long? = null,
)

private fun EvaluationSession.toCacheItem() = EvaluationCacheItem(
    id = id,
    title = title,
    domain = domain,
    description = description,
    completed = status == emy.partners.lawapp.domain.models.EvaluationStatus.Completed,
    progress = progress,
    score = score,
    questionCount = questionCount,
    completedQuestions = completedQuestions,
    duration = duration,
    updatedAt = updatedAt,
    level = level,
    canAnswer = canAnswer,
    alreadySubmitted = alreadySubmitted,
    startDate = startDate,
    endDate = endDate,
    compteurMinutes = compteurMinutes,
)

private fun EvaluationCacheItem.toSession() = EvaluationSession(
    id = id,
    title = title,
    domain = domain,
    description = description,
    status = if (completed || alreadySubmitted) {
        emy.partners.lawapp.domain.models.EvaluationStatus.Completed
    } else {
        emy.partners.lawapp.domain.models.EvaluationStatus.InProgress
    },
    progress = progress,
    score = score,
    questionCount = questionCount,
    completedQuestions = completedQuestions,
    duration = duration,
    updatedAt = updatedAt,
    level = level,
    canAnswer = canAnswer && !alreadySubmitted,
    alreadySubmitted = alreadySubmitted,
    startDate = startDate,
    endDate = endDate,
    compteurMinutes = compteurMinutes,
)
