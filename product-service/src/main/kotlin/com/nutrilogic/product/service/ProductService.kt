package com.nutrilogic.product.service

import com.nutrilogic.product.dto.ProductDto
import com.nutrilogic.product.entity.Product
import com.nutrilogic.product.exception.ProductNotFoundException
import com.nutrilogic.product.mapper.ProductMapper
import com.nutrilogic.product.repository.ProductRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
        existing.barcode = productDto.barcode
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

        val productsWithEfficiency = allProducts.map { product ->
            val nutrientAmount = extractNutrientAmount(product, nutrient)
            val efficiency = if (product.calories > 0) nutrientAmount / product.calories else 0.0
            product to efficiency
        }

        return productsWithEfficiency
            .sortedByDescending { it.second }
            .take(limit)
            .map { productMapper.toDto(it.first) }
    }

    private fun extractNutrientAmount(product: Product, nutrient: String): Double {
        return when (nutrient.lowercase()) {
            "calories" -> product.calories
            "protein" -> product.protein
            "fat" -> product.fat
            "carbs", "carbohydrates" -> product.carbs
            else -> {
                // Ищем в витаминах и минералах
                product.vitamins?.get(nutrient) ?:
                product.minerals?.get(nutrient) ?: 0.0
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