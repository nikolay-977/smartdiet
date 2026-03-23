package com.nutrilogic.recommendation.dto

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductDto(
    val id: Long,
    val name: String,
    val barcode: String? = null,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val vitamins: Map<String, Double>? = null,
    val minerals: Map<String, Double>? = null,
    val category: String? = null,
    val manufacturer: String? = null,
    val servingSize: Double = 100.0,
    val servingUnit: String = "g"
)