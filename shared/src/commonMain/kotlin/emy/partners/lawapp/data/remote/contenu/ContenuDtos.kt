package emy.partners.lawapp.data.remote.contenu

import emy.partners.lawapp.data.remote.ApiConfig
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ContenuListResponse(
    val contenu: List<ContenuFeedItemDto> = emptyList(),
)

@Serializable
data class ContenuFeedItemDto(
    val contenu: ContenuEntityDto? = null,
    val typeContenu: TypeContenuDto? = null,
    val scope: List<ScopeDto> = emptyList(),
    val user: ContenuUserDto? = null,
    val commentaires: List<ContenuCommentWrapperDto> = emptyList(),
    val likes: List<ContenuLikeWrapperDto> = emptyList(),
)

@Serializable
data class ContenuEntityDto(
    val id: Long? = null,
    val userId: Long? = null,
    val typeContenuId: Long? = null,
    val title: String? = null,
    val description: String? = null,
    val fileContent: String? = null,
    @SerialName("isActive")
    val active: Boolean? = null,
    val createdAt: String? = null,
)

@Serializable
data class TypeContenuDto(
    val id: Long? = null,
    val name: String? = null,
    val active: Boolean? = null,
)

@Serializable
data class ScopeDto(
    val id: Long? = null,
    val name: String? = null,
)

@Serializable
data class ContenuUserDto(
    val userId: Long? = null,
    val email: String? = null,
    val username: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    @SerialName("isPremium")
    val premium: Boolean? = null,
    @SerialName("isCertified")
    val certified: Boolean? = null,
) {
    val displayName: String
        get() {
            val full = listOfNotNull(
                firstName?.takeIf { it.isNotBlank() },
                lastName?.takeIf { it.isNotBlank() },
            ).joinToString(" ")
            return when {
                full.isNotBlank() -> full
                !username.isNullOrBlank() -> username.trimStart('@')
                !email.isNullOrBlank() -> email
                else -> "Utilisateur LawApp"
            }
        }
}

@Serializable
data class ContenuCommentWrapperDto(
    val commentaire: ContenuCommentDto? = null,
    val user: ContenuUserDto? = null,
)

@Serializable
data class ContenuCommentDto(
    val id: Long? = null,
    val contenuId: Long? = null,
    val userId: Long? = null,
    val description: String? = null,
    @SerialName("isActive")
    val active: Boolean? = null,
    val createdAt: String? = null,
)

@Serializable
data class ContenuLikeWrapperDto(
    val like: ContenuLikeDto? = null,
    val user: ContenuUserDto? = null,
)

@Serializable
data class ContenuLikeDto(
    val id: Long? = null,
    val contenuId: Long? = null,
    val like: Boolean? = null,
    val userId: Long? = null,
    @SerialName("isActive")
    val active: Boolean? = null,
    val createdAt: String? = null,
)

@Serializable
data class LikeContenuRequest(
    val contenuId: Long,
    val userId: Long,
)

data class ContenuFeedItem(
    val id: Long,
    val title: String,
    val description: String,
    val fileContent: String?,
    val authorName: String,
    val authorUsername: String,
    val likeCount: Int,
    val commentCount: Int,
    val comments: List<ContenuCommentUi>,
    val likedByMe: Boolean = false,
)

data class ContenuCommentUi(
    val id: Long,
    val text: String,
    val authorName: String,
)

fun ContenuFeedItemDto.toFeedItem(currentUserId: Long? = null): ContenuFeedItem? {
    val entity = contenu ?: return null
    val id = entity.id ?: return null
    val media = entity.fileContent?.trim()?.takeIf { it.isUsableMediaUrl() }
    val likedByMe = currentUserId != null && likes.any {
        it.like?.userId == currentUserId && it.like.like != false && it.like.active != false
    }
    return ContenuFeedItem(
        id = id,
        title = entity.title.orEmpty().ifBlank { "Sans titre" },
        description = entity.description.orEmpty(),
        fileContent = media?.let { resolveMediaUrl(it) },
        authorName = user?.displayName ?: "Utilisateur LawApp",
        authorUsername = user?.username?.let { if (it.startsWith("@")) it else "@$it" } ?: "@lawapp",
        likeCount = likes.count { it.like?.active != false && it.like?.like != false },
        commentCount = commentaires.count { it.commentaire?.active != false },
        comments = commentaires.mapNotNull { wrapper ->
            val comment = wrapper.commentaire ?: return@mapNotNull null
            ContenuCommentUi(
                id = comment.id ?: return@mapNotNull null,
                text = comment.description.orEmpty(),
                authorName = wrapper.user?.displayName ?: "Utilisateur",
            )
        },
        likedByMe = likedByMe,
    )
}

fun String.isUsableMediaUrl(): Boolean {
    val value = trim()
    if (value.isBlank()) return false
    val lowered = value.lowercase()
    if (lowered == "string" || lowered == "null" || lowered == "undefined") return false
    if (value.startsWith("http://") || value.startsWith("https://")) return true
    if (value.startsWith("/")) return true
    // Relative storage path sometimes returned without scheme.
    if (value.contains("/") && (lowered.endsWith(".png") || lowered.endsWith(".jpg") ||
            lowered.endsWith(".jpeg") || lowered.endsWith(".webp") || lowered.endsWith(".gif") ||
            lowered.endsWith(".mp4") || lowered.endsWith(".mov"))
    ) {
        return true
    }
    return false
}

fun resolveMediaUrl(raw: String): String {
    val value = raw.trim()
    return when {
        value.startsWith("http://") || value.startsWith("https://") -> value
        value.startsWith("/") -> "${ApiConfig.BASE_URL}$value"
        else -> "${ApiConfig.BASE_URL}/$value"
    }
}
