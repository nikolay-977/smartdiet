package ru.smartdiet.products.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.smartdiet.products.client.OpenFoodFactsClient
import ru.smartdiet.products.dto.ProductResponse

@Service
class ProductService(
    private val openFoodFactsClient: OpenFoodFactsClient
) {

    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    fun getProductInfo(barcode: String): ProductResponse {
        logger.info("Getting product info for barcode: {}", barcode)

        try {
            // 1. Валидация штрих-кода
            validateBarcode(barcode)

            // 2. Определяем источник (русский или мировой)
            val isRussianProduct = barcode.startsWith("46")
            logger.info("Product barcode {} is Russian: {}", barcode, isRussianProduct)

            // 3. Запрос к Open Food Facts
            val response = try {
                openFoodFactsClient.getProduct(barcode)
            } catch (e: Exception) {
                logger.warn("Russian API failed, trying worldwide API")
                // Если русский API не сработал, пробуем мировой
                getFromWorldwideApi(barcode)
            }

            // 4. Проверка статуса ответа
            val status = response["status"] as? Int ?: 0
            if (status != 1) {
                throw IllegalArgumentException("Продукт не найден в базе Open Food Facts")
            }

            // 5. Парсинг данных с учетом русского языка
            return parseProductData(response, barcode, isRussianProduct)

        } catch (e: Exception) {
            logger.error("Error getting product info: {}", e.message)
            throw RuntimeException("Не удалось получить информацию о продукте: ${e.message}", e)
        }
    }

    fun getRussianProductInfo(barcode: String): ProductResponse {
        logger.info("Getting Russian product info for barcode: {}", barcode)
        return getProductInfo(barcode)
    }

    private fun getFromWorldwideApi(barcode: String): Map<String, Any> {
        // Временное решение - используем мировой API
        // В реальном проекте нужно создать отдельный Feign Client для world.openfoodfacts.org
        val worldwideClient = createWorldwideClient()
        return try {
            worldwideClient.getProduct(barcode)
        } catch (e: Exception) {
            throw RuntimeException("Не удалось получить данные ни из русского, ни из мирового API")
        }
    }

    private fun createWorldwideClient(): OpenFoodFactsClient {
        // Здесь должна быть реализация Feign Client для мирового API
        // Пока используем тот же клиент, но с другим URL
        return openFoodFactsClient // Это упрощение, в реальности нужен отдельный клиент
    }

    private fun validateBarcode(barcode: String) {
        if (!barcode.matches("^[0-9]{8,13}$".toRegex())) {
            throw IllegalArgumentException("Неверный формат штрих-кода. Должно быть 8-13 цифр.")
        }
    }

    private fun parseProductData(
        response: Map<String, Any>,
        barcode: String,
        isRussianProduct: Boolean
    ): ProductResponse {
        val product = response["product"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val nutriments = product["nutriments"] as? Map<*, *> ?: emptyMap<Any, Any>()

        // Приоритет: русское название -> оригинальное название
        val nameRu = product["product_name_ru"] as? String
        val name = nameRu ?: (product["product_name"] as? String) ?: "Неизвестный продукт"

        // Приоритет: русские ингредиенты -> оригинальные ингредиенты
        val ingredientsRu = product["ingredients_text_ru"] as? String
        val ingredients = ingredientsRu ?: (product["ingredients_text"] as? String)

        // Приоритет: русские категории -> оригинальные категории
        val categoriesRu = product["categories_ru"] as? String
        val categories = categoriesRu ?: (product["categories"] as? String)

        return ProductResponse(
            barcode = barcode,
            name = name,
            nameRu = if (nameRu != null && nameRu != name) nameRu else null,
            brand = product["brands"] as? String,
            quantity = product["quantity"] as? String,

            // КБЖУ
            calories = parseDouble(nutriments["energy-kcal_100g"]),
            proteins = parseDouble(nutriments["proteins_100g"]),
            carbohydrates = parseDouble(nutriments["carbohydrates_100g"]),
            fats = parseDouble(nutriments["fat_100g"]),

            // Важные нутриенты
            fiber = parseDouble(nutriments["fiber_100g"]),
            calcium = parseDouble(nutriments["calcium_100g"]),
            omega3 = parseDouble(nutriments["omega-3-fat_100g"]) ?: parseDouble(nutriments["alpha-linolenic-acid_100g"]),
            saturatedFat = parseDouble(nutriments["saturated-fat_100g"]),
            iron = parseDouble(nutriments["iron_100g"]),
            sodium = parseDouble(nutriments["sodium_100g"]),
            potassium = parseDouble(nutriments["potassium_100g"]),
            choline = parseDouble(nutriments["choline_100g"]),
            caffeine = parseDouble(nutriments["caffeine_100g"]),

            // Дополнительная информация
            ingredients = ingredients,
            ingredientsRu = if (ingredientsRu != null && ingredientsRu != ingredients) ingredientsRu else null,
            categories = categories,
            categoriesRu = if (categoriesRu != null && categoriesRu != categories) categoriesRu else null,
            nutriscore = product["nutriscore_grade"] as? String,
            ecoscore = product["ecoscore_grade"] as? String,

            source = if (isRussianProduct) "ru.openfoodfacts.org" else "world.openfoodfacts.org"
        )
    }

    private fun parseDouble(value: Any?): Double? {
        return when (value) {
            is Double -> value
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is String -> {
                // Заменяем запятые на точки для русских чисел
                value.replace(',', '.').toDoubleOrNull()
            }
            else -> null
        }
    }
}