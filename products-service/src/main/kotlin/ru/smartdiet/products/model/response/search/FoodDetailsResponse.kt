package ru.smartdiet.products.model.response.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodDetailsResponse (
    val food: FoodDetails? = null
)

@Serializable
data class FoodDetails(
    @SerialName("food_id")
    val foodId: String? = null,
    @SerialName("food_name")
    val foodName: String? = null,
    @SerialName("food_type")
    val foodType: String? = null,
    @SerialName("food_url")
    val foodUrl: String? = null,
    val servings: ServingList? = null
)

@Serializable
data class Serving(
    @SerialName("serving_id")
    val servingId: String? = null,
    @SerialName("serving_description")
    val servingDescription: String? = null,
    @SerialName("serving_url")
    val servingUrl: String? = null,
    @SerialName("metric_serving_amount")
    val metricServingAmount: String? = null,
    @SerialName("metric_serving_unit")
    val metricServingUnit: String? = null,
    @SerialName("number_of_units")
    val numberOfUnits: String? = null,
    @SerialName("measurement_description")
    val measurementDescription: String? = null,
    @SerialName("is_default")
    val isDefault: String? = null,
    val calories: String? = null,
    val carbohydrate: String? = null,
    val protein: String? = null,
    val fat: String? = null,
    @SerialName("saturated_fat")
    val saturatedFat: String? = null,
    @SerialName("polyunsaturated_fat")
    val polyunsaturatedFat: String? = null,
    @SerialName("monounsaturated_fat")
    val monounsaturatedFat: String? = null,
    @SerialName("trans_fat")
    val transFat: String? = null,
    val cholesterol: String? = null,
    val sodium: String? = null,
    val potassium: String? = null,
    val fiber: String? = null,
    val sugar: String? = null,
    @SerialName("added_sugars")
    val addedSugars: String? = null,
    @SerialName("vitamin_d")
    val vitaminD: String? = null,
    @SerialName("vitamin_a")
    val vitaminA: String? = null,
    @SerialName("vitamin_c")
    val vitaminC: String? = null,
    val calcium: String? = null,
    val iron: String? = null
)

@Serializable
data class ServingList(
    val serving: List<Serving>? = null
)