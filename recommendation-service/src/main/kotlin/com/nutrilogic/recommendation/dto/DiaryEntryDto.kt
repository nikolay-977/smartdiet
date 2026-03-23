package com.nutrilogic.recommendation.dto

import java.time.LocalDate

data class DiaryEntryDto(
    val id: Long,
    val userId: Long,
    val entryDate: LocalDate,
    val consumedProducts: List<ConsumedProductDto>,
    val totalCalories: Double,
    val totalProtein: Double,
    val totalFat: Double,
    val totalCarbs: Double
)