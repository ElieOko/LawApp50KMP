package emy.partners.lawapp.data.remote.student

import emy.partners.lawapp.data.local.LocalStore
import emy.partners.lawapp.data.local.createLocalStore
import emy.partners.lawapp.data.remote.auth.AuthRepository
import emy.partners.lawapp.data.remote.createHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class StudentApi(
    private val client: HttpClient = createHttpClient(),
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
    },
) {
    suspend fun getPromotions(): Result<List<NamedOption>> = runCatching {
        val response = client.get("/api/v1/public/promotions")
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw StudentApiException(extractMessage(body) ?: "Chargement des promotions impossible")
        }
        json.decodeFromString(PromotionsResponse.serializer(), body).promotions
            .mapNotNull { item ->
                val id = item.id ?: return@mapNotNull null
                val name = item.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                if (item.active == false) return@mapNotNull null
                NamedOption(id = id, name = name)
            }
    }

    suspend fun getEtablissements(): Result<List<NamedOption>> = runCatching {
        val response = client.get("/api/v1/public/etablissements")
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw StudentApiException(extractMessage(body) ?: "Chargement des etablissements impossible")
        }
        json.decodeFromString(EtablissementsResponse.serializer(), body).etablissements
            .mapNotNull { item ->
                val id = item.id ?: return@mapNotNull null
                val name = item.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                NamedOption(id = id, name = name)
            }
    }

    suspend fun createStudent(request: StudentRequest): Result<Unit> = runCatching {
        val response = client.post("/api/v1/public/students") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw StudentApiException(extractMessage(body) ?: "Enregistrement etudiant impossible")
        }
    }

    private fun extractMessage(bodyText: String): String? {
        if (bodyText.isBlank()) return null
        return runCatching {
            json.parseToJsonElement(bodyText).jsonObject["message"]?.jsonPrimitive?.content
        }.getOrNull()
    }
}

class StudentApiException(message: String) : Exception(message)

object StudentRepository {
    private const val KEY_STUDENT_DONE_PREFIX = "lawapp_student_profile_done_"

    private val api = StudentApi()
    private val store: LocalStore by lazy { createLocalStore() }

    fun isStudentProfileCompleted(userId: Long?): Boolean {
        if (userId == null) return false
        return store.getString(KEY_STUDENT_DONE_PREFIX + userId) == "1"
    }

    fun markStudentProfileCompleted(userId: Long) {
        store.putString(KEY_STUDENT_DONE_PREFIX + userId, "1")
    }

    suspend fun loadPromotions(): Result<List<NamedOption>> = api.getPromotions()

    suspend fun loadEtablissements(): Result<List<NamedOption>> = api.getEtablissements()

    suspend fun completeStudentProfile(
        promotionId: Long,
        etablissementId: Long?,
        matricule: String?,
        gender: String?,
    ): Result<Unit> {
        val userId = AuthRepository.currentSession?.profile?.userId
            ?: return Result.failure(StudentApiException("Connectez-vous pour completer le profil etudiant."))
        return api.createStudent(
            StudentRequest(
                userId = userId,
                promotionId = promotionId,
                etablissementId = etablissementId,
                matricule = matricule?.trim()?.takeIf { it.isNotBlank() },
                gender = gender?.trim()?.takeIf { it.isNotBlank() },
            )
        ).onSuccess {
            markStudentProfileCompleted(userId)
        }
    }
}
