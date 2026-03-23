package com.nutrilogic.recommendation.mapper

import com.nutrilogic.recommendation.dto.DiaryEntryDto
import com.nutrilogic.recommendation.entity.DiaryEntry
import org.springframework.stereotype.Component

@Component
class DiaryEntryMapper(
    private val consumedProductMapper: ConsumedProductMapper
) {

    fun toDto(entry: DiaryEntry): DiaryEntryDto {
        return DiaryEntryDto(
            id = entry.id!!,
            userId = entry.user.id!!,
            entryDate = entry.entryDate,
            consumedProducts = entry.consumedProducts.map { consumedProductMapper.toDto(it) },
            totalCalories = entry.totalCalories,
            totalProtein = entry.totalProtein,
            totalFat = entry.totalFat,
            totalCarbs = entry.totalCarbs
        )
    }

    fun toDtoList(entries: List<DiaryEntry>): List<DiaryEntryDto> = entries.map { toDto(it) }
}