package com.nutrilogic.recommendation.repository

import com.nutrilogic.recommendation.entity.ConsumedProduct
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ConsumedProductRepository : JpaRepository<ConsumedProduct, Long> {
    fun findByDiaryEntryId(diaryEntryId: Long): List<ConsumedProduct>
    fun deleteByDiaryEntryId(diaryEntryId: Long)
}