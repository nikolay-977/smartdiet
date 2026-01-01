package ru.smartdiet.products

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ru.smartdiet.products.model.response.auth.AccessTokenResponse
import ru.smartdiet.products.model.response.error.FatSecretError
import ru.smartdiet.products.model.response.error.OAuthError
import ru.smartdiet.products.model.response.search.FoodDetailsResponse
import ru.smartdiet.products.model.response.search.FoodSearchResponseV1
import ru.smartdiet.products.model.response.search.FoodsV1
import java.util.concurrent.atomic.AtomicReference

class FatSecretService(
    private val clientId: String,
    private val clientSecret: String
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                explicitNulls = false
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
            sanitizeHeader { header ->
                header == HttpHeaders.Authorization || header.contains("client")
            }
        }

        expectSuccess = false
    }

    private val oauthUrl = "https://oauth.fatsecret.com/connect/token"
    private val serverApiUrl = "https://platform.fatsecret.com/rest/server.api"

    private var accessToken: AtomicReference<String?> = AtomicReference(null)
    private var tokenExpiryTime: AtomicReference<Long> = AtomicReference(0)

    private suspend fun getAccessToken(): String {
        val currentToken = accessToken.get()
        val expiryTime = tokenExpiryTime.get()

        // Проверяем, есть ли действующий токен
        if (currentToken != null && System.currentTimeMillis() < expiryTime) {
            println("Using cached access token")
            return currentToken
        }

        println("Requesting new access token from FatSecret...")

        val response: HttpResponse = httpClient.post(oauthUrl) {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                "grant_type=client_credentials&scope=basic&client_id=$clientId&client_secret=$clientSecret"
            )
        }

        println("Token response status: ${response.status}")

        if (response.status.isSuccess()) {
            val tokenResponse = response.body<AccessTokenResponse>()
            accessToken.set(tokenResponse.accessToken)
            // Устанавливаем время истечения (вычитаем 60 секунд для запаса)
            val expiresInMillis = tokenResponse.expiresIn * 1000L
            tokenExpiryTime.set(
                System.currentTimeMillis() + expiresInMillis - 60000
            )
            println("Access token received, expires in ${tokenResponse.expiresIn} seconds")
            return tokenResponse.accessToken
        } else {
            val error = try {
                response.body<OAuthError>()
            } catch (e: Exception) {
                OAuthError("unknown", response.bodyAsText())
            }
            val errorMessage = "Failed to get access token: ${error.errorDescription ?: error.error}"
            println(errorMessage)
            println("Request body was: grant_type=client_credentials&scope=basic&client_id=$clientId&client_secret=[HIDDEN]")
            throw RuntimeException(errorMessage)
        }
    }

    suspend fun searchFoodsV1(
        searchExpression: String,
        maxResults: Int = 20,
        pageNumber: Int = 0,
        format: String = "json",
        genericDescription: String? = null,
        region: String = "US",
        language: String? = null
    ): Result<FoodSearchResponseV1> {
        println("Searching for food using v1 API: '$searchExpression', maxResults=$maxResults, pageNumber=$pageNumber")
        val token = getAccessToken()

        try {
            val response: HttpResponse = httpClient.get(serverApiUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                accept(ContentType.Application.Json)
                parameter("method", "foods.search")
                parameter("search_expression", searchExpression)
                parameter("max_results", maxResults.toString())
                parameter("page_number", pageNumber.toString())
                parameter("format", format)
                parameter("region", region)

                if (!genericDescription.isNullOrEmpty()) {
                    parameter("generic_description", genericDescription)
                }
                if (!language.isNullOrEmpty()) {
                    parameter("language", language)
                }
            }

            println("Food search v1 response status: ${response.status}")

            if (response.status.isSuccess()) {
                try {
                    val responseText = response.bodyAsText()
                    println("Raw v1 search response: $responseText")

                    val json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    }

                    val body = try {
                        json.decodeFromString<FoodSearchResponseV1>(responseText)
                    } catch (e: Exception) {
                        println("Error parsing v1 response: ${e.message}")
                        // Создаем пустой ответ
                        FoodSearchResponseV1(
                            foods = FoodsV1(
                                maxResults = maxResults.toString(),
                                totalResults = "0",
                                pageNumber = pageNumber.toString(),
                                food = emptyList()
                            )
                        )
                    }

                    println("Successfully processed v1 search response, found ${body.foods?.food?.size ?: 0} items")
                    return Result.success(body)
                } catch (e: Exception) {
                    println("Error parsing successful v1 response: ${e.message}")
                    e.printStackTrace()
                    return Result.failure(e)
                }
            } else {
                try {
                    val errorResponse = response.body<FatSecretError>()
                    val errorCode = errorResponse.error?.code ?: "UNKNOWN"
                    val errorMessage = errorResponse.error?.message ?: response.bodyAsText()
                    println("FatSecret v1 API error: $errorCode - $errorMessage")

                    return Result.failure(
                        RuntimeException("FatSecret API error $errorCode: $errorMessage")
                    )
                } catch (e: Exception) {
                    val responseText = response.bodyAsText()
                    println("Failed to parse error response: ${e.message}")
                    println("Raw error response: $responseText")

                    return Result.failure(
                        RuntimeException("HTTP ${response.status.value}: $responseText")
                    )
                }
            }
        } catch (e: Exception) {
            println("Error during food search v1 API call: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    suspend fun getFoodDetails(
        foodId: String,
        format: String = "json"
    ): Result<FoodDetailsResponse> {
        println("Getting food details for ID: $foodId")
        val token = getAccessToken()

        try {
            val response: HttpResponse = httpClient.get(serverApiUrl) {
                header(HttpHeaders.Authorization, "Bearer $token")
                accept(ContentType.Application.Json)
                parameter("method", "food.get")
                parameter("food_id", foodId)
                parameter("format", format)
            }

            println("Food details response status: ${response.status}")

            if (response.status.isSuccess()) {
                try {
                    val responseText = response.bodyAsText()
                    println("Raw food details response: $responseText")

                    val json = Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        coerceInputValues = true
                    }

                    val body = try {
                        json.decodeFromString<FoodDetailsResponse>(responseText)
                    } catch (e: Exception) {
                        println("Error parsing food details response: ${e.message}")
                        FoodDetailsResponse(food = null)
                    }

                    println("Successfully processed food details response")
                    return Result.success(body)
                } catch (e: Exception) {
                    println("Error parsing successful food details response: ${e.message}")
                    e.printStackTrace()
                    return Result.failure(e)
                }
            } else {
                try {
                    val errorResponse = response.body<FatSecretError>()
                    val errorCode = errorResponse.error?.code ?: "UNKNOWN"
                    val errorMessage = errorResponse.error?.message ?: response.bodyAsText()
                    println("FatSecret API error: $errorCode - $errorMessage")

                    return Result.failure(
                        RuntimeException("FatSecret API error $errorCode: $errorMessage")
                    )
                } catch (e: Exception) {
                    val responseText = response.bodyAsText()
                    println("Failed to parse error response: ${e.message}")
                    println("Raw error response: $responseText")

                    return Result.failure(
                        RuntimeException("HTTP ${response.status.value}: $responseText")
                    )
                }
            }
        } catch (e: Exception) {
            println("Error during food details API call: ${e.message}")
            e.printStackTrace()
            return Result.failure(e)
        }
    }

    fun close() {
        println("Closing FatSecretService")
        httpClient.close()
    }
}