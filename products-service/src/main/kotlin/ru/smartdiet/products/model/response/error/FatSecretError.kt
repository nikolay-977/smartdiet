package ru.smartdiet.products.model.response.error

import kotlinx.serialization.Serializable

@Serializable
data class FatSecretError(
    val error: FatSecretErrorDetail? = null
)

@Serializable
data class FatSecretErrorDetail(
    val code: String? = null,
    val message: String? = null
)