package io.almer.api

@kotlinx.serialization.Serializable
data class User(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val admin: Boolean
)