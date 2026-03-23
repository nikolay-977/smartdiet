package com.nutrilogic.recommendation.dto

import java.time.LocalDate

data class ConsumedProductDto(
    val id: Long? = null,
    val productId: Long,
    val quantity: Double,
    val date: LocalDate? = null,
    val name: String? = null,
    val calories: Double? = null,
    val protein: Double? = null,
    val fat: Double? = null,
    val carbs: Double? = null
)