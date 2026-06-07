package emy.partners.lawapp.domain.models

data class Category(
    val id : Long,
    val name : String,
    var isActive : Boolean = false
)
