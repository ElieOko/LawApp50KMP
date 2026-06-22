package emy.partners.lawapp.domain.models

enum class ContentDestination {
    Home,
    Explore
}

data class ContentAttachment(
    val name: String,
    val uri: String,
    val mimeType: String? = null
)

data class UserGeneratedContent(
    val id: Long,
    val destination: ContentDestination,
    val title: String,
    val description: String,
    val author: String,
    val link: String? = null,
    val attachment: ContentAttachment? = null,
    val createdAt: String = "A l'instant"
)

data class UserGeneratedContentDraft(
    val destination: ContentDestination,
    val title: String,
    val description: String,
    val author: String,
    val link: String? = null,
    val attachment: ContentAttachment? = null
)

