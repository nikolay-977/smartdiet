package ru.smartdiet.products.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.smartdiet.products.client.OpenFoodFactsClient
import ru.smartdiet.products.dto.ProductResponse
import java.time.LocalDateTime

@Service
class ProductService(
    private val openFoodFactsClient: OpenFoodFactsClient
) {

    private val logger = LoggerFactory.getLogger(ProductService::class.java)

    fun getProductInfo(barcode: String): ProductResponse {
        logger.info("Getting product info for barcode: {}", barcode)

        // 1. Валидация штрих-кода
        if (!isValidBarcode(barcode)) {
            throw IllegalArgumentException("Invalid barcode format. Must be 8-13 digits.")
        }

        // 2. Запрос к Open Food Facts API
        val response = openFoodFactsClient.getProduct(barcode)

        // 3. Проверка статуса ответа
        val status = response["status"] as? Int ?: 0
        if (status != 1) {
            throw IllegalArgumentException("Product not found in Open Food Facts database")
        }

        // 4. Извлечение данных
        val product = response["product"] as? Map<*, *>

        if (product == null) {
            throw IllegalArgumentException("Invalid product data received")
        }

        // 5. Парсинг нутриентов
        val nutriments = product["nutriments"] as? Map<*, *>

        return ProductResponse(
            barcode = barcode,
            name = (product["product_name"] as? String) ?: "Unknown Product",
            brand = product["brands"] as? String,
            quantity = product["quantity"] as? String,
            calories = nutriments?.get("energy-kcal_100g") as? Double,
            proteins = nutriments?.get("proteins_100g") as? Double,
            carbohydrates = nutriments?.get("carbohydrates_100g") as? Double,
            fats = nutriments?.get("fat_100g") as? Double,
            sugars = nutriments?.get("sugars_100g") as? Double
        )
    }

    private fun isValidBarcode(barcode: String): Boolean {
        return barcode.matches("^[0-9]{8,13}$".toRegex())
    }
}