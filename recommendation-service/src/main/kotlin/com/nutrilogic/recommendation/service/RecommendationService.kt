package com.nutrilogic.recommendation.service

import com.nutrilogic.recommendation.client.ProductServiceClient
import com.nutrilogic.recommendation.dto.NutrientTargetDto
import com.nutrilogic.recommendation.dto.ProductDto
import com.nutrilogic.recommendation.repository.UserPreferenceRepository
import org.springframework.stereotype.Service

@Service
class RecommendationService(
    private val productServiceClient: ProductServiceClient,
    private val preferenceRepository: UserPreferenceRepository
) {

    fun recommendByNutrient(
        userId: Long,
        nutrient: String,
        limit: Int = 7
    ): List<ProductDto> {
        val bannedProductIds = preferenceRepository.findBannedProductIds(userId)

        val recommended = productServiceClient.getRecommendedProducts(nutrient, limit)

        return recommended.filter { it.id !in bannedProductIds }
    }

    fun generateMealPlan(
        userId: Long,
        targets: List<NutrientTargetDto>
    ): List<List<ProductDto>> {
        val bannedProductIds = preferenceRepository.findBannedProductIds(userId)
        val favoriteProductIds = preferenceRepository.findFavoriteProductIds(userId)

        val allProducts = productServiceClient.getAllProducts()
            .filter { it.id !in bannedProductIds }

        // Сортировка: сначала любимые, затем остальные
        val sortedProducts = allProducts.sortedByDescending {
            if (it.id in favoriteProductIds) 1 else 0
        }

        // Простая реализация: возвращаем 3 варианта плана
        return listOf(
            sortedProducts.take(5),
            sortedProducts.drop(5).take(5),
            sortedProducts.drop(10).take(5)
        )
    }
}