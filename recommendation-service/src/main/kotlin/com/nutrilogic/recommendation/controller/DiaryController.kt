package com.nutrilogic.recommendation.controller

import com.nutrilogic.recommendation.dto.AddProductRequest
import com.nutrilogic.recommendation.dto.DiaryEntryDto
import com.nutrilogic.recommendation.dto.UserDto
import com.nutrilogic.recommendation.service.DiaryService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/diary")
@PreAuthorize("hasRole('USER')")
class DiaryController(
    private val diaryService: DiaryService
) {

    @PostMapping
    fun addProduct(
        @AuthenticationPrincipal user: UserDto,
        @RequestBody request: AddProductRequest
    ): DiaryEntryDto {
        return diaryService.addProduct(user.id!!, request)
    }

    @GetMapping
    fun getDiary(
        @AuthenticationPrincipal user: UserDto,
        @RequestParam(required = false) date: LocalDate?
    ): DiaryEntryDto? {
        return diaryService.getDiaryEntry(user.id!!, date ?: LocalDate.now())
    }

    @GetMapping("/history")
    fun getUserDiary(
        @AuthenticationPrincipal user: UserDto
    ): List<DiaryEntryDto> {
        return diaryService.getUserDiary(user.id!!)
    }

    @DeleteMapping("/products/{consumedProductId}")
    fun deleteProduct(
        @AuthenticationPrincipal user: UserDto,
        @PathVariable consumedProductId: Long
    ) {
        diaryService.deleteProduct(consumedProductId, user.id!!)
    }
}