package com.nutrilogic.recommendation.dto

data class NutrientTargetDto(
    val nutrientName: String,
    val targetValue: Double,
    val unit: String = "g"
)