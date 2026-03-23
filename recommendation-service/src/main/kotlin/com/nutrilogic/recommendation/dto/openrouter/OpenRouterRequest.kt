package com.nutrilogic.recommendation.dto.openrouter

import com.fasterxml.jackson.annotation.JsonProperty

data class OpenRouterRequest(
    val model: String,
    val messages: List<Message>,
    @JsonProperty("max_tokens")
    val maxTokens: Int? = null,
    val temperature: Double? = null,
    @JsonProperty("top_p")
    val topP: Double? = null,
    val stream: Boolean = false
)

data class Message(
    val role: String,
    val content: String
)