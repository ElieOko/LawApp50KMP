package emy.partners.lawapp.data.remote.classroom

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

class ClassroomApi(
    private val client: HttpClient = createHttpClient(),
    private val json: Json = AppJson,
) {
    suspend fun listMine(accessToken: String): Result<List<ClassroomDto>> = runCatching {
        authorizedGet(accessToken, "/api/v1/private/classrooms/mine").decodeClassrooms()
    }

    suspend fun listAll(accessToken: String): Result<List<ClassroomDto>> = runCatching {
        authorizedGet(accessToken, "/api/v1/protected/classrooms").decodeClassrooms()
    }

    suspend fun create(accessToken: String, request: ClassroomRequest): Result<ClassroomDto> = runCatching {
        val response = client.post {
            url.takeFrom("${ApiConfig.BASE_URL}/api/v1/private/classrooms")
            withBearerToken(accessToken)
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw ClassroomApiException(json.extractMessage(body) ?: "Creation de salle impossible")
        }
        body.decodeClassrooms().firstOrNull()
            ?: json.extractObject(body, "classroom", "classe", "data")?.let {
                runCatching { json.decodeFromJsonElement(ClassroomDto.serializer(), it) }.getOrNull()
            }
            ?: throw ClassroomApiException("Salle creee mais reponse illisible")
    }

    suspend fun listUsers(accessToken: String, classroomId: Long): Result<List<ClassroomUserDto>> = runCatching {
        val body = authorizedGet(accessToken, "/api/v1/private/classrooms/$classroomId/users")
        json.extractArray(body, "users", "classroomUsers", "data", "content", "items")
            .mapNotNull { element ->
                runCatching { json.decodeFromJsonElement(ClassroomUserDto.serializer(), element) }.getOrNull()
            }
    }

    suspend fun addUser(accessToken: String, classroomId: Long, userId: Long): Result<Unit> = runCatching {
        val response = client.post {
            url.takeFrom("${ApiConfig.BASE_URL}/api/v1/private/classrooms/users")
            withBearerToken(accessToken)
            contentType(ContentType.Application.Json)
            setBody(ClassroomUserRequest(classroomId = classroomId, userId = userId))
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw ClassroomApiException(json.extractMessage(body) ?: "Ajout a la salle impossible")
        }
    }

    private suspend fun authorizedGet(accessToken: String, path: String): String {
        val response = client.get {
            url.takeFrom("${ApiConfig.BASE_URL}$path")
            withBearerToken(accessToken)
        }
        val body = response.bodyAsText()
        if (!response.status.isSuccess()) {
            throw ClassroomApiException(json.extractMessage(body) ?: "Chargement des salles impossible")
        }
        return body
    }

    private fun String.decodeClassrooms(): List<ClassroomDto> =
        json.extractArray(this, "classrooms", "classes", "data", "content", "items")
            .mapNotNull { element ->
                runCatching { json.decodeFromJsonElement(ClassroomDto.serializer(), element) }.getOrNull()
            }
}

class ClassroomApiException(message: String) : Exception(message)

object ClassroomRepository {
    private val api = ClassroomApi()

    private var memoryMine: List<ClassroomSummary>? = null

    fun cachedMine(): List<ClassroomSummary> = memoryMine.orEmpty()

    suspend fun loadMine(forceRefresh: Boolean = false): Result<List<ClassroomSummary>> {
        if (!forceRefresh) {
            memoryMine?.let { return Result.success(it) }
        }
        val token = requireToken()
            ?: return Result.failure(ClassroomApiException("Connectez-vous pour gerer les salles."))
        return api.listMine(token).map { items ->
            items.mapNotNull { it.toSummary() }.also { memoryMine = it }
        }
    }

    suspend fun createClassroom(name: String, description: String?): Result<ClassroomSummary> {
        val token = requireToken()
            ?: return Result.failure(ClassroomApiException("Connectez-vous pour creer une salle."))
        val trimmed = name.trim()
        if (trimmed.isBlank()) {
            return Result.failure(ClassroomApiException("Le nom de la salle est obligatoire."))
        }
        return api.create(token, ClassroomRequest(name = trimmed, description = description?.trim())).mapCatching { dto ->
            dto.toSummary() ?: throw ClassroomApiException("Salle creee mais reponse illisible")
        }.onSuccess { created ->
            memoryMine = listOf(created) + cachedMine().filter { it.id != created.id }
        }
    }

    suspend fun loadMembers(classroomId: Long): Result<List<ClassroomMember>> {
        val token = requireToken()
            ?: return Result.failure(ClassroomApiException("Connectez-vous pour voir la presence."))
        return api.listUsers(token, classroomId).map { items ->
            items.mapNotNull { dto ->
                val id = dto.id ?: return@mapNotNull null
                val userId = dto.userId ?: return@mapNotNull null
                ClassroomMember(
                    id = id,
                    userId = userId,
                    classroomId = dto.classroomId ?: classroomId,
                    present = dto.active != false,
                )
            }
        }
    }

    suspend fun addMember(classroomId: Long, userId: Long): Result<Unit> {
        val token = requireToken()
            ?: return Result.failure(ClassroomApiException("Connectez-vous pour ajouter un membre."))
        if (userId <= 0L) {
            return Result.failure(ClassroomApiException("Identifiant utilisateur invalide."))
        }
        return api.addUser(token, classroomId, userId)
    }

    suspend fun isCurrentUserInClassroom(classroomId: Long): Boolean {
        val userId = AuthRepository.currentSession?.profile?.userId ?: return false
        return loadMembers(classroomId).getOrNull()?.any { it.userId == userId && it.present } == true
    }

    private fun requireToken(): String? =
        AuthRepository.currentSession?.accessToken?.takeIf { it.isNotBlank() }
}

private fun ClassroomDto.toSummary(): ClassroomSummary? {
    val classroomId = id ?: return null
    return ClassroomSummary(
        id = classroomId,
        name = name?.takeIf { it.isNotBlank() } ?: "Salle $classroomId",
        description = description.orEmpty(),
        ownerId = ownerId,
    )
}
