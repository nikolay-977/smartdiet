package com.nutrilogic.recommendation.dto

import java.time.LocalDate

data class AddProductRequest(
    val productId: Long,
    val quantity: Double,
    val date: LocalDate? = null
)