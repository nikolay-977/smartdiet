package com.nutrilogic.recommendation.dto

data class AuthResponse(
    val token: String,
    val user: UserDto
)