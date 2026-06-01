package emy.partners.lawapp.domain.models

import org.jetbrains.compose.resources.DrawableResource

data class Article(
    val articleId : Long,
    val author : String,
    val profile: DrawableResource,
    val content : String,
    val file : DrawableResource,
    val extra : ExtraContent,
    var comments : List<Comment> =  emptyList(),
    val image : String = "https://upeswebsitecdn-prod-hphqfhc0b8h2ffhf.a02.azurefd.net/drupal-data/2024-11/Emerging%20Legal%20Specializations-%20What%20to%20Focus%20on%20During%20Your%20LLB.jpeg",
    val isPlay : Boolean = false,
    val video : String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
)

data class ExtraContent(
    val like : Int = 0,
    val comment : Int = 0,
    val favorite : Int = 0,
    val share : Int = 0
)