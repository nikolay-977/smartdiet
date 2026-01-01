package ru.smartdiet.products.model.response.error

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuthError(
    val error: String,
    @SerialName("error_description")
    val errorDescription: String?
)