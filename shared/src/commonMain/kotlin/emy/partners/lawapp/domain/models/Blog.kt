package emy.partners.lawapp.domain.models

import org.jetbrains.compose.resources.DrawableResource

data class Blog(
    val id : Long,
    val title : String,
    val background : DrawableResource,
    val description : String = "",
    val isActive : Boolean = false,
    val author : User,
    val type : String= "Article"
)
