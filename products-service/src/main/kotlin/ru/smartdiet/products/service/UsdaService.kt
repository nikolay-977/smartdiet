package ru.smartdiet.products.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import ru.smartdiet.products.client.UsdaClient
import ru.smartdiet.products.dto.ProductResponse
import ru.smartdiet.products.dto.UsdaFood
import ru.smartdiet.products.dto.UsdaNutrient

@Service
class UsdaService(
    private val usdaClient: UsdaClient
) {

    private val logger = LoggerFactory.getLogger(UsdaService::class.java)

    @Value("\${usda.api.key:DEMO_KEY}")
    private lateinit var usdaApiKey: String

    fun searchProducts(query: String): List<ProductResponse> {
        logger.info("Searching USDA products for query: {}", query)

        return try {
            val response = usdaClient.searchFoods(
                apiKey = usdaApiKey,
                query = query,
                pageSize = 10,
                pageNumber = 1
            )

            response.foods?.mapNotNull { usdaFood ->
                convertUsdaFoodToProductResponse(usdaFood)
            } ?: emptyList()

        } catch (e: Exception) {
            logger.error("Error searching USDA products: {}", e.message, e)
            throw RuntimeException("Не удалось выполнить поиск в USDA: ${e.message}", e)
        }
    }

    private fun convertUsdaFoodToProductResponse(usdaFood: UsdaFood): ProductResponse {
        val nutrients = usdaFood.foodNutrients ?: emptyList()

        return ProductResponse(
            barcode = usdaFood.fdcId.toString(), // Используем fdcId как временный штрих-код
            name = usdaFood.description,
            brand = usdaFood.brandName ?: usdaFood.brandOwner,
            quantity = if (usdaFood.servingSize != null && usdaFood.servingSizeUnit != null) {
                "${usdaFood.servingSize} ${usdaFood.servingSizeUnit}"
            } else {
                null
            },

            // КБЖУ - находим соответствующие нутриенты
            calories = findNutrientValue(nutrients, "Energy"),
            proteins = findNutrientValue(nutrients, "Protein"),
            carbohydrates = findNutrientValue(nutrients, "Carbohydrate, by difference"),
            fats = findNutrientValue(nutrients, "Total lipid (fat)"),

            // Другие нутриенты
            fiber = findNutrientValue(nutrients, "Fiber, total dietary"),
            calcium = findNutrientValue(nutrients, "Calcium, Ca"),
            saturatedFat = findNutrientValue(nutrients, "Fatty acids, total saturated"),
            iron = findNutrientValue(nutrients, "Iron, Fe"),
            sodium = findNutrientValue(nutrients, "Sodium, Na"),
            potassium = findNutrientValue(nutrients, "Potassium, K"),
            choline = findNutrientValue(nutrients, "Choline, total"),

            // Для USDA нет этих полей, оставляем null
            omega3 = null,
            caffeine = null,
            ingredients = null,
            ingredientsRu = null,
            categories = usdaFood.foodCategory,
            categoriesRu = null,
            nutriscore = null,
            ecoscore = null,

            source = "USDA"
        )
    }

    private fun findNutrientValue(nutrients: List<UsdaNutrient>, name: String): Double? {
        return nutrients.find { it.nutrientName.contains(name, ignoreCase = true) }?.value
    }
}