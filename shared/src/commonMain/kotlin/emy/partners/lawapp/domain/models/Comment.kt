package emy.partners.lawapp.domain.models

data class Comment(
    val id : Long,
    val comment : String,
    val user : User
)

