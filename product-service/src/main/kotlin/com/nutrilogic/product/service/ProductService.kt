package com.nutrilogic.product.service

import com.nutrilogic.product.dto.ProductDto
import com.nutrilogic.product.entity.Product
import com.nutrilogic.product.exception.ProductNotFoundException
import com.nutrilogic.product.mapper.ProductMapper
import com.nutrilogic.product.repository.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Locale

@Service
@Transactional
class ProductService(
    private val productRepository: ProductRepository,
    private val productMapper: ProductMapper
) {

    fun createProduct(productDto: ProductDto): ProductDto {
        val product = productMapper.toEntity(productDto)
        val saved = productRepository.save(product)
        return productMapper.toDto(saved)
    }

    @Transactional(readOnly = true)
    fun getAllProducts(page: Int = 0, size: Int = 100): List<ProductDto> {
        val pageable = PageRequest.of(page, size)
        return productMapper.toDtoList(productRepository.findAll(pageable).content)
    }

    @Transactional(readOnly = true)
    fun getProductById(id: Long): ProductDto {
        val product = productRepository.findById(id).orElseThrow { ProductNotFoundException(id) }
        return productMapper.toDto(product)
    }

    fun updateProduct(id: Long, productDto: ProductDto): ProductDto {
        val existing = productRepository.findById(id).orElseThrow { ProductNotFoundException(id) }

        existing.name = productDto.name
        existing.calories = productDto.calories
        existing.protein = productDto.protein
        existing.fat = productDto.fat
        existing.carbs = productDto.carbs
        existing.category = productDto.category
        existing.manufacturer = productDto.manufacturer
        existing.servingSize = productDto.servingSize
        existing.servingUnit = productDto.servingUnit
        existing.vitamins = productDto.vitamins
        existing.minerals = productDto.minerals

        val updated = productRepository.save(existing)
        return productMapper.toDto(updated)
    }

    fun deleteProduct(id: Long) {
        if (!productRepository.existsById(id)) {
            throw ProductNotFoundException(id)
        }
        productRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun searchByName(name: String, page: Int = 0, size: Int = 100): List<ProductDto> {
        val pageable = PageRequest.of(page, size)
        return productMapper.toDtoList(productRepository.findByNameContainingIgnoreCase(name, pageable).content)
    }

    @Transactional(readOnly = true)
    fun getRecommendedProductsByNutrient(
        nutrient: String,
        limit: Int = 7
    ): List<ProductDto> {
        val allProducts = productRepository.findAll()

        println("=== Getting recommendations for nutrient: $nutrient ===")

        val productsWithEfficiency = allProducts.map { product ->
            val nutrientAmount = extractNutrientAmount(product, nutrient)
            val efficiency = if (product.calories > 0) nutrientAmount / product.calories else 0.0

            // Отладка - выводим информацию для всех продуктов
            if (nutrientAmount > 0) {
                println("${product.name}: $nutrient = $nutrientAmount mg, Calories = ${product.calories}, Efficiency = ${String.format("%.3f", efficiency)}")
            }

            product to efficiency
        }

        val sorted = productsWithEfficiency
            .sortedByDescending { it.second }
            .take(limit)

        println("=== Top $limit products ===")
        sorted.forEachIndexed { index, (product, efficiency) ->
            println("${index + 1}. ${product.name} - Efficiency: ${String.format("%.3f", efficiency)}")
        }

        return sorted.map { productMapper.toDto(it.first) }
    }

    private fun extractNutrientAmount(product: Product, nutrient: String): Double {
        // Очищаем от кавычек и лишних пробелов
        val cleanedNutrient = nutrient.trim().removeSurrounding("\"", "'")
        val normalizedNutrient = cleanedNutrient.lowercase()

        return when (normalizedNutrient) {
            "calories" -> product.calories
            "protein" -> product.protein
            "fat" -> product.fat
            "carbs", "carbohydrates" -> product.carbs
            else -> {
                val searchKey = normalizeKeyForMap(normalizedNutrient)

                println("---")
                println("Product: ${product.name}")
                println("  Looking for: '$searchKey' (from nutrient: '$cleanedNutrient')")
                println("  Minerals keys: ${product.minerals?.keys ?: "null"}")
                println("  Vitamins keys: ${product.vitamins?.keys ?: "null"}")

                // 1. Пробуем точное совпадение в минералах и витаминах
                var value = product.minerals?.get(searchKey) ?: product.vitamins?.get(searchKey)

                // 2. Поиск по частичному совпадению в минералах (для "Iron, Fe" -> "Iron")
                if (value == null && product.minerals != null) {
                    value = product.minerals!!.entries.firstOrNull { entry ->
                        entry.key.contains(searchKey, ignoreCase = true) ||
                                searchKey.contains(entry.key.split(",")[0].trim(), ignoreCase = true)
                    }?.value

                    if (value != null) {
                        println("  Found mineral by partial match: ${product.minerals?.entries?.find { it.value == value }?.key} -> $value")
                    }
                }

                // 3. Поиск по частичному совпадению в витаминах
                if (value == null && product.vitamins != null) {
                    value = product.vitamins!!.entries.firstOrNull {
                        it.key.contains(searchKey, ignoreCase = true) ||
                                searchKey.contains(it.key, ignoreCase = true)
                    }?.value

                    if (value != null) {
                        println("  Found vitamin by partial match: ${product.vitamins?.entries?.find { it.value == value }?.key} -> $value")
                    }
                }

                println("  Result: ${value ?: 0.0}")

                value ?: 0.0
            }
        }
    }

    private fun normalizeKeyForMap(nutrient: String): String {
        val lower = nutrient.lowercase()
        return when (lower) {
            // Минералы
            "calcium", "ca" -> "Calcium"
            "iron", "fe" -> "Iron"
            "magnesium", "mg" -> "Magnesium"
            "potassium", "k" -> "Potassium"
            "zinc", "zn" -> "Zinc"
            "copper", "cu" -> "Copper"
            "manganese", "mn" -> "Manganese"
            "phosphorus", "p" -> "Phosphorus"
            "selenium", "se" -> "Selenium"
            "sodium", "na" -> "Sodium"

            // Витамины
            "vitamina", "vitamin a" -> "Vitamin A"
            "vitaminc", "vitamin c" -> "Vitamin C"
            "vitamind", "vitamin d" -> "Vitamin D"
            "vitamine", "vitamin e" -> "Vitamin E"
            "vitamink", "vitamin k" -> "Vitamin K"
            "vitaminb6", "vitamin b6", "b6" -> "Vitamin B6"
            "vitaminb12", "vitamin b12", "b12" -> "Vitamin B12"
            "thiamin", "vitamin b1", "b1" -> "Vitamin B1"
            "riboflavin", "vitamin b2", "b2" -> "Vitamin B2"
            "niacin", "vitamin b3", "b3" -> "Vitamin B3"
            "folate" -> "Folate"

            else -> {
                val default = lower.replaceFirstChar { it.uppercase() }
                default
            }
        }
    }

    @Transactional(readOnly = true)
    fun getProductsByIds(ids: List<Long>): List<ProductDto> {
        return productMapper.toDtoList(productRepository.findAllById(ids))
    }

    @Transactional(readOnly = true)
    fun getTotalCount(): Long = productRepository.countAll()
}