package ru.smartdiet.products.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class UsdaSearchResponse(
    @JsonProperty("foods")
    val foods: List<UsdaFood>? = emptyList(),
    @JsonProperty("totalHits")
    val totalHits: Int = 0,
    @JsonProperty("currentPage")
    val currentPage: Int = 0,
    @JsonProperty("totalPages")
    val totalPages: Int = 0
)

data class UsdaFood(
    @JsonProperty("fdcId")
    val fdcId: Long,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("brandName")
    val brandName: String? = null,
    @JsonProperty("foodNutrients")
    val foodNutrients: List<UsdaNutrient>? = emptyList(),
    @JsonProperty("servingSize")
    val servingSize: Double? = null,
    @JsonProperty("servingSizeUnit")
    val servingSizeUnit: String? = null,
    @JsonProperty("foodCategory")
    val foodCategory: String? = null,
    @JsonProperty("dataType")
    val dataType: String? = null,
    @JsonProperty("publishedDate")
    val publishedDate: String? = null,
    @JsonProperty("marketCountry")
    val marketCountry: String? = null,
    @JsonProperty("brandOwner")
    val brandOwner: String? = null
)

data class UsdaNutrient(
    @JsonProperty("nutrientId")
    val nutrientId: Int,
    @JsonProperty("nutrientName")
    val nutrientName: String,
    @JsonProperty("nutrientNumber")
    val nutrientNumber: String,
    @JsonProperty("unitName")
    val unitName: String,
    @JsonProperty("value")
    val value: Double,
    @JsonProperty("derivationCode")
    val derivationCode: String? = null,
    @JsonProperty("derivationDescription")
    val derivationDescription: String? = null
)