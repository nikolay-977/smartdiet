package com.nutrilogic.recommendation.repository

import com.nutrilogic.recommendation.entity.DiaryEntry
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

@Repository
interface DiaryEntryRepository : JpaRepository<DiaryEntry, Long> {
    fun findByUserIdAndEntryDate(userId: Long, entryDate: LocalDate): Optional<DiaryEntry>
    fun findByUserIdOrderByEntryDateDesc(userId: Long): List<DiaryEntry>
}