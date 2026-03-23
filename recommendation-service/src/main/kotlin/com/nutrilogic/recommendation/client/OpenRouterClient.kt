package com.nutrilogic.recommendation.client

import com.nutrilogic.recommendation.dto.openrouter.Message
import com.nutrilogic.recommendation.dto.openrouter.OpenRouterRequest
import com.nutrilogic.recommendation.dto.openrouter.OpenRouterResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Duration

@Component
class OpenRouterClient(
    @Value("\${openrouter.api-key}") private val apiKey: String,
    @Value("\${openrouter.base-url}") private val baseUrl: String,
    @Value("\${openrouter.site-url:}") private val siteUrl: String,
    @Value("\${openrouter.site-name:NutriLogic}") private val siteName: String
) {

    private val webClient: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("Authorization", "Bearer $apiKey")
        .defaultHeader("Content-Type", "application/json")
        .defaultHeader("HTTP-Referer", siteUrl)
        .defaultHeader("X-Title", siteName)
        .build()

    fun chatCompletion(
        model: String,
        messages: List<Message>,
        maxTokens: Int? = null,
        temperature: Double? = null,
        topP: Double? = null
    ): OpenRouterResponse {
        val request = OpenRouterRequest(
            model = model,
            messages = messages,
            maxTokens = maxTokens,
            temperature = temperature,
            topP = topP,
            stream = false
        )

        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(OpenRouterResponse::class.java)
            .timeout(Duration.ofSeconds(30))
            .block() ?: throw RuntimeException("Failed to get response from OpenRouter")
    }

    fun chatCompletionAsync(
        model: String,
        messages: List<Message>,
        maxTokens: Int? = null,
        temperature: Double? = null,
        topP: Double? = null
    ): Mono<OpenRouterResponse> {
        val request = OpenRouterRequest(
            model = model,
            messages = messages,
            maxTokens = maxTokens,
            temperature = temperature,
            topP = topP,
            stream = false
        )

        return webClient.post()
            .uri("/chat/completions")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(OpenRouterResponse::class.java)
            .timeout(Duration.ofSeconds(30))
    }
}