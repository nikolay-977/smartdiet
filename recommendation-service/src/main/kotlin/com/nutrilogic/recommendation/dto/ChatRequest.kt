package com.nutrilogic.recommendation.dto

data class ChatRequest(
    val message: String,
    val sessionId: Long? = null
)