package ru.smartdiet.products.model.response.search

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FoodSearchResponseV1(
    val foods: FoodsV1? = null
)

@Serializable
data class FoodsV1(
    @SerialName("max_results")
    val maxResults: String? = null,
    @SerialName("total_results")
    val totalResults: String? = null,
    @SerialName("page_number")
    val pageNumber: String? = null,
    val food: List<FoodV1>? = null
)

@Serializable
data class FoodV1(
    @SerialName("food_id")
    val foodId: String? = null,
    @SerialName("food_name")
    val foodName: String? = null,
    @SerialName("brand_name")
    val brandName: String? = null,
    @SerialName("food_type")
    val foodType: String? = null,
    @SerialName("food_url")
    val foodUrl: String? = null,
    @SerialName("food_description")
    val foodDescription: String? = null
)