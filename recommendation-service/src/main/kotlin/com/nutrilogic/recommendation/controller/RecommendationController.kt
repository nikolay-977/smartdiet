package com.nutrilogic.recommendation.controller

import com.nutrilogic.recommendation.dto.NutrientTargetDto
import com.nutrilogic.recommendation.dto.ProductDto
import com.nutrilogic.recommendation.dto.UserDto
import com.nutrilogic.recommendation.service.RecommendationService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/recommendations")
@PreAuthorize("hasRole('USER')")
class RecommendationController(
    private val recommendationService: RecommendationService
) {

    @GetMapping("/nutrient")
    fun recommendByNutrient(
        @AuthenticationPrincipal user: UserDto,
        @RequestParam nutrient: String,
        @RequestParam(defaultValue = "7") limit: Int
    ): List<ProductDto> {
        return recommendationService.recommendByNutrient(user.id!!, nutrient, limit)
    }

    @PostMapping("/meal-plan")
    fun generateMealPlan(
        @AuthenticationPrincipal user: UserDto,
        @RequestBody targets: List<NutrientTargetDto>
    ): List<List<ProductDto>> {
        return recommendationService.generateMealPlan(user.id!!, targets)
    }
}