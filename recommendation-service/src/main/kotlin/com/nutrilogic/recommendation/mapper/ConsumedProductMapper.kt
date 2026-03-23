package com.nutrilogic.recommendation.mapper

import com.nutrilogic.recommendation.dto.ConsumedProductDto
import com.nutrilogic.recommendation.entity.ConsumedProduct
import org.springframework.stereotype.Component

@Component
class ConsumedProductMapper {

    fun toDto(consumed: ConsumedProduct): ConsumedProductDto {
        return ConsumedProductDto(
            id = consumed.id,
            productId = consumed.productId,
            quantity = consumed.quantity,
            name = consumed.name,
            calories = consumed.calories,
            protein = consumed.protein,
            fat = consumed.fat,
            carbs = consumed.carbs
        )
    }

    fun toEntity(dto: ConsumedProductDto, diaryEntryId: Long): ConsumedProduct {
        return ConsumedProduct(
            diaryEntry = null,
            productId = dto.productId,
            quantity = dto.quantity,
            name = dto.name,
            calories = dto.calories,
            protein = dto.protein,
            fat = dto.fat,
            carbs = dto.carbs
        )
    }
}