package emy.partners.lawapp.data.remote.teacher

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class TeacherTypeDto(
    val id: Long? = null,
    val name: String? = null,
    @JsonNames("isActive")
    val active: Boolean? = null,
)

@Serializable
data class TeacherTypesResponse(
    val teacherTypes: List<TeacherTypeDto> = emptyList(),
)

@Serializable
data class EtablissementRefRequest(
    @SerialName("etablisementId")
    val etablissementId: Long,
)

@Serializable
data class TeacherRequest(
    val typeTeacherId: Long,
    val departement: String? = null,
    val justificatif: String? = null,
    val gender: String? = null,
    val etablissement: List<EtablissementRefRequest> = emptyList(),
)

@Serializable
data class TeacherDto(
    val teacherId: Long? = null,
    val typeTeacherId: Long? = null,
    val departement: String? = null,
    val justificatif: String? = null,
    val gender: String? = null,
)
