package io.almer.api

@kotlinx.serialization.Serializable
data class UserPayload(
    val firstName: String,
    val lastName: String,
    val admin: Boolean
)