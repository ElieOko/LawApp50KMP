package emy.partners.lawapp.data.remote.classroom

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ClassroomDto(
    val id: Long? = null,
    val name: String? = null,
    val description: String? = null,
    val ownerId: Long? = null,
    val createdAt: String? = null,
    @JsonNames("isActive")
    val active: Boolean? = null,
)

@Serializable
data class ClassroomRequest(
    val name: String,
    val description: String? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ClassroomUserDto(
    val id: Long? = null,
    val classroomId: Long? = null,
    val userId: Long? = null,
    val createdAt: String? = null,
    @JsonNames("isActive")
    val active: Boolean? = null,
)

@Serializable
data class ClassroomUserRequest(
    val classroomId: Long,
    val userId: Long,
)

data class ClassroomSummary(
    val id: Long,
    val name: String,
    val description: String,
    val ownerId: Long?,
    val memberCount: Int = 0,
)

data class ClassroomMember(
    val id: Long,
    val userId: Long,
    val classroomId: Long,
    val present: Boolean,
)
