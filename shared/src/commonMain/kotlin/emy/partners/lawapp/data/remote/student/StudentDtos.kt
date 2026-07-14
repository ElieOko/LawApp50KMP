package emy.partners.lawapp.data.remote.student

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PromotionsResponse(
    val promotions: List<PromotionDto> = emptyList(),
)

@Serializable
data class EtablissementsResponse(
    val etablissements: List<EtablissementDto> = emptyList(),
)

@Serializable
data class PromotionDto(
    val id: Long? = null,
    val name: String? = null,
    @SerialName("isActive")
    val active: Boolean? = null,
)

@Serializable
data class EtablissementDto(
    val id: Long? = null,
    val name: String? = null,
    @SerialName("isActive")
    val active: Boolean? = null,
)

@Serializable
data class StudentRequest(
    val userId: Long,
    val promotionId: Long,
    val etablissementId: Long? = null,
    val matricule: String? = null,
    val gender: String? = null,
)

data class NamedOption(
    val id: Long,
    val name: String,
)
