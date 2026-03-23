package com.nutrilogic.product.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.nutrilogic.product.dto.ImportResponse
import com.nutrilogic.product.entity.Product
import com.nutrilogic.product.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

@Service
class ImportService(
    private val productRepository: ProductRepository,
    private val objectMapper: ObjectMapper
) {

    private var lastImportStatus: ImportStatus = ImportStatus()
    private var totalProductsCount: Long = 0

    @Transactional
    fun importFoodDataFromJson(file: MultipartFile): ImportResponse {
        val startTime = System.currentTimeMillis()

        return try {
            validateFile(file)

            // Используем потоковую обработку для больших файлов
            val jsonContent = String(file.bytes)
            val foodData = parseJson(jsonContent)

            // Используем пакетную вставку для улучшения производительности
            val batchSize = 100
            val products = convertToProducts(foodData)

            var importedCount = 0
            products.chunked(batchSize).forEach { batch ->
                productRepository.saveAll(batch)
                importedCount += batch.size
            }

            totalProductsCount = productRepository.count()
            lastImportStatus = ImportStatus(
                success = true,
                importedCount = importedCount.toLong(),
                deletedCount = 0,
                totalCount = totalProductsCount,
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                timestamp = LocalDateTime.now()
            )

            ImportResponse(
                success = true,
                message = "Successfully imported $importedCount products",
                importedCount = importedCount,
                totalCount = totalProductsCount,
                processingTimeMs = lastImportStatus.endTime - lastImportStatus.startTime
            )
        } catch (e: Exception) {
            lastImportStatus = ImportStatus(
                success = false,
                error = e.message,
                startTime = startTime,
                endTime = System.currentTimeMillis(),
                timestamp = LocalDateTime.now()
            )

            ImportResponse(
                success = false,
                message = "Import failed: ${e.message}",
                importedCount = 0,
                totalCount = productRepository.count(),
                processingTimeMs = lastImportStatus.endTime - lastImportStatus.startTime,
                error = e.message
            )
        }
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("File is empty")
        }

        val contentType = file.contentType
        if (contentType != "application/json" && !file.originalFilename?.endsWith(".json", ignoreCase = true)!!) {
            throw IllegalArgumentException("File must be JSON")
        }

        if (file.size > 100 * 1024 * 1024) {
            throw IllegalArgumentException("File size exceeds 100 MB limit")
        }
    }

    private fun parseJson(jsonContent: String): FoodDataResponse {
        val root = objectMapper.readValue<Map<String, Any>>(jsonContent)
        val foundationFoods = root["FoundationFoods"] as? List<Map<String, Any>>
            ?: throw IllegalArgumentException("Invalid JSON structure: 'FoundationFoods' not found")

        return FoodDataResponse(foods = foundationFoods)
    }

    private fun convertToProducts(foodData: FoodDataResponse): List<Product> {
        return foodData.foods.mapNotNull { foodJson ->
            try {
                convertToProduct(foodJson)
            } catch (e: Exception) {
                println("Failed to parse product: ${foodJson["description"]}, error: ${e.message}")
                null
            }
        }
    }

    private fun convertToProduct(foodJson: Map<String, Any>): Product {
        val description = foodJson["description"] as? String ?: "Unknown"
        val nutrients = foodJson["foodNutrients"] as? List<Map<String, Any>> ?: emptyList()

        var calories = 0.0
        var protein = 0.0
        var fat = 0.0
        var carbs = 0.0

        val vitamins = mutableMapOf<String, Double>()
        val minerals = mutableMapOf<String, Double>()

        nutrients.forEach { nutrientMap ->
            val nutrient = nutrientMap["nutrient"] as? Map<String, Any> ?: return@forEach
            val name = nutrient["name"] as? String ?: return@forEach
            val unitName = nutrient["unitName"] as? String ?: ""
            val amount = (nutrientMap["amount"] as? Number)?.toDouble() ?: return@forEach

            when {
                // Энергия (калории)
                name.equals("Energy", ignoreCase = true) && unitName.equals("kcal", ignoreCase = true) -> {
                    calories = amount
                }
                // Белок
                name.equals("Protein", ignoreCase = true) -> {
                    protein = amount
                }
                // Жиры
                name.equals("Total lipid (fat)", ignoreCase = true) -> {
                    fat = amount
                }
                // Углеводы
                name.equals("Carbohydrate, by difference", ignoreCase = true) -> {
                    carbs = amount
                }
                // Витамины (исключаем токоферолы и токотриенолы)
                name.startsWith("Vitamin", ignoreCase = true) &&
                        !name.contains("Tocopherol", ignoreCase = true) &&
                        !name.contains("Tocotrienol", ignoreCase = true) -> {
                    vitamins[normalizeNutrientName(name)] = amount
                }
                // Минералы
                isMineral(name) -> {
                    minerals[normalizeNutrientName(name)] = amount
                }
            }
        }

        val foodCategory = (foodJson["foodCategory"] as? Map<String, Any>)?.get("description") as? String
        val category = foodCategory ?: "Other"

        if (calories == 0.0) {
            println("Warning: Product '$description' has 0 calories")
        }

        return Product(
            name = description,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            vitamins = vitamins.ifEmpty { null },
            minerals = minerals.ifEmpty { null },
            category = category
        )
    }

    private fun isMineral(name: String): Boolean {
        val minerals = listOf(
            "Calcium", "Iron", "Magnesium", "Phosphorus", "Potassium",
            "Sodium", "Zinc", "Copper", "Manganese", "Selenium",
            "Iodine", "Chromium", "Molybdenum", "Fluoride"
        )
        return minerals.any { name.startsWith(it, ignoreCase = true) }
    }

    // Нормализация названий нутриентов
    private fun normalizeNutrientName(name: String): String {
        return when {
            // Витамины - нормализуем к простому виду
            name.contains("Vitamin C", ignoreCase = true) -> "Vitamin C"
            name.contains("Vitamin A", ignoreCase = true) -> "Vitamin A"
            name.contains("Vitamin D", ignoreCase = true) -> "Vitamin D"
            name.contains("Vitamin E", ignoreCase = true) -> "Vitamin E"
            name.contains("Vitamin K", ignoreCase = true) -> "Vitamin K"
            name.contains("Vitamin B-6", ignoreCase = true) -> "Vitamin B6"
            name.contains("Vitamin B-12", ignoreCase = true) -> "Vitamin B12"
            name.contains("Thiamin", ignoreCase = true) -> "Vitamin B1"
            name.contains("Riboflavin", ignoreCase = true) -> "Vitamin B2"
            name.contains("Niacin", ignoreCase = true) -> "Vitamin B3"
            name.contains("Folate", ignoreCase = true) -> "Folate"

            // Для минералов - если есть запятая, сохраняем как есть
            name.contains(",") -> {
                // Оставляем оригинальное название, но убираем лишние пробелы
                name.split(",").joinToString(", ") { it.trim() }
            }

            // Простые минералы
            name.startsWith("Calcium", ignoreCase = true) -> "Calcium"
            name.startsWith("Iron", ignoreCase = true) -> "Iron"
            name.startsWith("Magnesium", ignoreCase = true) -> "Magnesium"
            name.startsWith("Potassium", ignoreCase = true) -> "Potassium"
            name.startsWith("Zinc", ignoreCase = true) -> "Zinc"
            name.startsWith("Copper", ignoreCase = true) -> "Copper"
            name.startsWith("Manganese", ignoreCase = true) -> "Manganese"
            name.startsWith("Phosphorus", ignoreCase = true) -> "Phosphorus"
            name.startsWith("Selenium", ignoreCase = true) -> "Selenium"
            name.startsWith("Sodium", ignoreCase = true) -> "Sodium"

            else -> name
        }
    }

    fun getLastImportStatus(): Map<String, Any?> {
        return mapOf(
            "success" to lastImportStatus.success,
            "timestamp" to lastImportStatus.timestamp,
            "importedCount" to lastImportStatus.importedCount,
            "totalCount" to lastImportStatus.totalCount,
            "deletedCount" to lastImportStatus.deletedCount,
            "error" to lastImportStatus.error,
            "processingTimeMs" to (lastImportStatus.endTime - lastImportStatus.startTime)
        )
    }

    fun getTotalProductsCount(): Long = productRepository.count()

    data class ImportStatus(
        var success: Boolean = false,
        var importedCount: Long = 0,
        var deletedCount: Long = 0,
        var totalCount: Long = 0,
        var startTime: Long = 0,
        var endTime: Long = 0,
        var timestamp: LocalDateTime = LocalDateTime.now(),
        var error: String? = null,
        var shouldClearExisting: Boolean = true
    )

    data class FoodDataResponse(
        val foods: List<Map<String, Any>>
    )
}