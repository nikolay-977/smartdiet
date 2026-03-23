package com.nutrilogic.recommendation.controller

import com.nutrilogic.recommendation.dto.UserDto
import com.nutrilogic.recommendation.service.PreferenceService
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/preferences")
@PreAuthorize("hasRole('USER')")
class PreferenceController(
    private val preferenceService: PreferenceService
) {

    @PostMapping("/favorites/{productId}")
    fun addToFavorites(
        @AuthenticationPrincipal user: UserDto,
        @PathVariable productId: Long
    ) {
        preferenceService.addToFavorites(user.id!!, productId)
    }

    @DeleteMapping("/favorites/{productId}")
    fun removeFromFavorites(
        @AuthenticationPrincipal user: UserDto,
        @PathVariable productId: Long
    ) {
        preferenceService.removeFromFavorites(user.id!!, productId)
    }

    @PostMapping("/stop-list/{productId}")
    fun addToStopList(
        @AuthenticationPrincipal user: UserDto,
        @PathVariable productId: Long
    ) {
        preferenceService.addToStopList(user.id!!, productId)
    }

    @DeleteMapping("/stop-list/{productId}")
    fun removeFromStopList(
        @AuthenticationPrincipal user: UserDto,
        @PathVariable productId: Long
    ) {
        preferenceService.removeFromStopList(user.id!!, productId)
    }

    @GetMapping("/favorites")
    fun getFavorites(
        @AuthenticationPrincipal user: UserDto
    ): List<Long> {
        return preferenceService.getFavorites(user.id!!)
    }

    @GetMapping("/stop-list")
    fun getStopList(
        @AuthenticationPrincipal user: UserDto
    ): List<Long> {
        return preferenceService.getStopList(user.id!!)
    }
}