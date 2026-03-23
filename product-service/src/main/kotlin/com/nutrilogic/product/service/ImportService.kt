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
            val amount = (nutrientMap["amount"] as? Number)?.toDouble() ?: return@forEach

            when {
                name.contains("Energy", ignoreCase = true) && name.contains("kcal", ignoreCase = true) -> {
                    calories = amount
                }
                name.equals("Protein", ignoreCase = true) -> {
                    protein = amount
                }
                name.equals("Total lipid (fat)", ignoreCase = true) -> {
                    fat = amount
                }
                name.equals("Carbohydrate, by difference", ignoreCase = true) -> {
                    carbs = amount
                }
                name.contains("Vitamin", ignoreCase = true) -> {
                    vitamins[name] = amount
                }
                name.contains("Iron", ignoreCase = true) ||
                        name.contains("Calcium", ignoreCase = true) ||
                        name.contains("Magnesium", ignoreCase = true) ||
                        name.contains("Potassium", ignoreCase = true) ||
                        name.contains("Zinc", ignoreCase = true) ||
                        name.contains("Selenium", ignoreCase = true) ||
                        name.contains("Copper", ignoreCase = true) ||
                        name.contains("Manganese", ignoreCase = true) -> {
                    minerals[name] = amount
                }
            }
        }

        val foodCategory = (foodJson["foodCategory"] as? Map<String, Any>)?.get("description") as? String
        val category = foodCategory ?: "Other"

        return Product(
            name = description,
            barcode = null,
            calories = calories,
            protein = protein,
            fat = fat,
            carbs = carbs,
            vitamins = if (vitamins.isNotEmpty()) vitamins else null,
            minerals = if (minerals.isNotEmpty()) minerals else null,
            category = category
        )
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