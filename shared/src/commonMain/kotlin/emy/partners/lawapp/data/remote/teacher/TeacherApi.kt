package emy.partners.lawapp.data.remote.teacher

import emy.partners.lawapp.data.local.LocalStore
import emy.partners.lawapp.data.local.createLocalStore
import emy.partners.lawapp.data.remote.ApiConfig
import emy.partners.lawapp.data.remote.AppJson
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.data.remote.createHttpClient
import emy.partners.lawapp.data.remote.extractArray
import emy.partners.lawapp.data.remote.extractMessage
import emy.partners.lawapp.data.remote.student.NamedOption
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

class TeacherApi(
    private val client: HttpClient = createHttpClient(),
    private val json: Json = AppJson,
) {
    suspend fun getTeacherTypes(): Result<List<NamedOption>> = runCatching {
        val response = client.get("/api/v1/public/teacher-types")
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw TeacherApiException(json.extractMessage(body) ?: "Chargement des types enseignant impossible")
        }
        val decoded = runCatching {
            json.decodeFromString(TeacherTypesResponse.serializer(), body).teacherTypes
        }.getOrDefault(emptyList())
        val items = decoded.ifEmpty {
            json.extractArray(body, "teacherTypes", "data", "types").mapNotNull { element ->
                runCatching { json.decodeFromJsonElement(TeacherTypeDto.serializer(), element) }.getOrNull()
            }
        }
        items.mapNotNull { item ->
            val id = item.id ?: return@mapNotNull null
            val name = item.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            if (item.active == false) return@mapNotNull null
            NamedOption(id = id, name = name)
        }
    }

    suspend fun createTeacher(accessToken: String, request: TeacherRequest): Result<Unit> = runCatching {
        val paths = listOf("/v1/private/teachers", "/api/v1/private/teachers")
        var lastError: String? = null
        for (path in paths) {
            val response = client.post {
                url.takeFrom("${ApiConfig.BASE_URL}$path")
                withBearerToken(accessToken)
                contentType(ContentType.Application.Json)
                setBody(request)
            }
            val body = response.bodyAsText()
            if (response.status.isSuccess()) return@runCatching
            lastError = json.extractMessage(body) ?: "Enregistrement enseignant impossible (${response.status.value})"
            if (response.status.value != 404) {
                throw TeacherApiException(lastError)
            }
        }
        throw TeacherApiException(lastError ?: "Enregistrement enseignant impossible")
    }
}

class TeacherApiException(message: String) : Exception(message)

object TeacherRepository {
    private const val KEY_TEACHER_DONE_PREFIX = "lawapp_teacher_profile_done_"

    private val api = TeacherApi()
    private val store: LocalStore by lazy { createLocalStore() }

    fun isTeacherAuthorized(): Boolean =
        AuthRepository.currentSession?.profile?.isTeacher == true

    fun isTeacherProfileCompleted(userId: Long?): Boolean {
        if (userId == null) return false
        return store.getString(KEY_TEACHER_DONE_PREFIX + userId) == "1"
    }

    fun markTeacherProfileCompleted(userId: Long) {
        store.putString(KEY_TEACHER_DONE_PREFIX + userId, "1")
    }

    suspend fun loadTeacherTypes(): Result<List<NamedOption>> = api.getTeacherTypes()

    suspend fun completeTeacherProfile(
        typeTeacherId: Long,
        departement: String?,
        justificatif: String?,
        gender: String?,
        etablissementId: Long?,
    ): Result<Unit> {
        val session = AuthRepository.currentSession
            ?: return Result.failure(TeacherApiException("Connectez-vous pour completer le profil enseignant."))
        val token = session.accessToken.takeIf { it.isNotBlank() }
            ?: return Result.failure(TeacherApiException("Connectez-vous pour completer le profil enseignant."))
        val userId = session.profile?.userId
            ?: return Result.failure(TeacherApiException("Identifiant utilisateur manquant."))
        return api.createTeacher(
            accessToken = token,
            request = TeacherRequest(
                typeTeacherId = typeTeacherId,
                departement = departement?.trim()?.takeIf { it.isNotBlank() },
                justificatif = justificatif?.trim()?.takeIf { it.isNotBlank() },
                gender = normalizeGender(gender),
                etablissement = listOfNotNull(
                    etablissementId?.let { EtablissementRefRequest(etablissementId = it) }
                ),
            ),
        ).onSuccess {
            markTeacherProfileCompleted(userId)
        }
    }

    private fun normalizeGender(value: String?): String? {
        val raw = value?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return when {
            raw.equals("M", ignoreCase = true) ||
                raw.equals("Masculin", ignoreCase = true) ||
                raw.equals("Male", ignoreCase = true) -> "M"
            raw.equals("F", ignoreCase = true) ||
                raw.equals("Feminin", ignoreCase = true) ||
                raw.equals("Féminin", ignoreCase = true) ||
                raw.equals("Female", ignoreCase = true) -> "F"
            else -> raw.take(1).uppercase()
        }
    }
}
