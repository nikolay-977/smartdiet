package ru.smartdiet.products.model.response.error

import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    val code: Int,
    val message: String,
    val details: String? = null
)