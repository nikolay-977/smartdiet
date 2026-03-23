package com.nutrilogic.product.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero

data class ProductDto(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:PositiveOrZero(message = "Calories must be positive or zero")
    val calories: Double = 0.0,

    @field:PositiveOrZero(message = "Protein must be positive or zero")
    val protein: Double = 0.0,

    @field:PositiveOrZero(message = "Fat must be positive or zero")
    val fat: Double = 0.0,

    @field:PositiveOrZero(message = "Carbs must be positive or zero")
    val carbs: Double = 0.0,

    val vitamins: Map<String, Double>? = null,

    val minerals: Map<String, Double>? = null,

    val category: String? = null,

    val manufacturer: String? = null,

    val servingSize: Double = 100.0,

    val servingUnit: String = "g"
)