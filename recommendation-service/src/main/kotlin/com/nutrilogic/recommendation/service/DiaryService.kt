package com.nutrilogic.recommendation.service

import com.nutrilogic.recommendation.client.ProductServiceClient
import com.nutrilogic.recommendation.dto.AddProductRequest
import com.nutrilogic.recommendation.dto.DiaryEntryDto
import com.nutrilogic.recommendation.entity.ConsumedProduct
import com.nutrilogic.recommendation.entity.DiaryEntry
import com.nutrilogic.recommendation.exception.DiaryEntryNotFoundException
import com.nutrilogic.recommendation.mapper.DiaryEntryMapper
import com.nutrilogic.recommendation.repository.ConsumedProductRepository
import com.nutrilogic.recommendation.repository.DiaryEntryRepository
import com.nutrilogic.recommendation.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional
class DiaryService(
    private val diaryEntryRepository: DiaryEntryRepository,
    private val consumedProductRepository: ConsumedProductRepository,
    private val userRepository: UserRepository,
    private val productServiceClient: ProductServiceClient,
    private val diaryEntryMapper: DiaryEntryMapper
) {

    fun addProduct(userId: Long, request: AddProductRequest): DiaryEntryDto {
        val user = userRepository.findById(userId).orElseThrow()
        val date = request.date ?: LocalDate.now()

        // Получаем продукт из Product Service
        val product = productServiceClient.getProduct(request.productId)

        // Находим или создаем запись дневника за этот день
        val diaryEntry = diaryEntryRepository.findByUserIdAndEntryDate(userId, date)
            .orElseGet {
                DiaryEntry(user = user, entryDate = date).also {
                    diaryEntryRepository.save(it)
                }
            }

        // Создаем запись о потребленном продукте
        val consumedProduct = ConsumedProduct(
            diaryEntry = diaryEntry,
            productId = product.id,
            quantity = request.quantity,
            name = product.name,
            calories = product.calories,
            protein = product.protein,
            fat = product.fat,
            carbs = product.carbs
        )

        diaryEntry.consumedProducts.add(consumedProduct)
        recalculateTotals(diaryEntry)

        diaryEntryRepository.save(diaryEntry)
        return diaryEntryMapper.toDto(diaryEntry)
    }

    private fun recalculateTotals(diaryEntry: DiaryEntry) {
        diaryEntry.totalCalories = diaryEntry.consumedProducts.sumOf {
            (it.calories ?: 0.0) * it.quantity / 100.0
        }
        diaryEntry.totalProtein = diaryEntry.consumedProducts.sumOf {
            (it.protein ?: 0.0) * it.quantity / 100.0
        }
        diaryEntry.totalFat = diaryEntry.consumedProducts.sumOf {
            (it.fat ?: 0.0) * it.quantity / 100.0
        }
        diaryEntry.totalCarbs = diaryEntry.consumedProducts.sumOf {
            (it.carbs ?: 0.0) * it.quantity / 100.0
        }
    }

    fun getDiaryEntry(userId: Long, date: LocalDate): DiaryEntryDto? {
        return diaryEntryRepository.findByUserIdAndEntryDate(userId, date)
            .map(diaryEntryMapper::toDto)
            .orElse(null)
    }

    fun getUserDiary(userId: Long): List<DiaryEntryDto> {
        return diaryEntryMapper.toDtoList(
            diaryEntryRepository.findByUserIdOrderByEntryDateDesc(userId)
        )
    }

    fun deleteProduct(consumedProductId: Long, userId: Long) {
        val consumedProduct = consumedProductRepository.findById(consumedProductId)
            .orElseThrow { DiaryEntryNotFoundException() }

        if (consumedProduct.diaryEntry!!.user.id != userId) {
            throw DiaryEntryNotFoundException("Product not found in user's diary")
        }

        val diaryEntry = consumedProduct.diaryEntry
        diaryEntry!!.consumedProducts.remove(consumedProduct)
        consumedProductRepository.delete(consumedProduct)

        recalculateTotals(diaryEntry)
        diaryEntryRepository.save(diaryEntry)
    }
}