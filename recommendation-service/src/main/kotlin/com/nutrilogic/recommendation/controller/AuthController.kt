package com.nutrilogic.recommendation.controller

import com.nutrilogic.recommendation.dto.AuthRequest
import com.nutrilogic.recommendation.dto.AuthResponse
import com.nutrilogic.recommendation.service.AuthService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/login")
    fun login(
        @RequestBody(required = false) request: AuthRequest,
    ): AuthResponse {
        return authService.login(request)
    }

    @PostMapping("/register")
    fun register(
        @RequestBody(required = false) request: AuthRequest?,
    ): AuthResponse {
        return authService.register(request!!.username, request.password)
    }
}