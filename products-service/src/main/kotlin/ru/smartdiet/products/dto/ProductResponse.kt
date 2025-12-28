package ru.smartdiet.products.dto

import java.time.LocalDateTime

data class ProductResponse(
    val barcode: String,
    val name: String,
    val brand: String?,
    val quantity: String?,
    val calories: Double?,
    val proteins: Double?,
    val carbohydrates: Double?,
    val fats: Double?,
    val sugars: Double?,
    val retrievedAt: LocalDateTime = LocalDateTime.now()
)

data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val path: String? = null
)