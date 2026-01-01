package ru.smartdiet.products

import com.typesafe.config.ConfigFactory
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.config.HoconApplicationConfig
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.uri
import org.slf4j.event.Level
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import ru.smartdiet.products.model.response.error.ApiError
import ru.smartdiet.products.model.response.info.ApiInfo
import ru.smartdiet.products.model.response.info.EndpointInfo
import kotlin.onSuccess

private val log = LoggerFactory.getLogger("Main")

fun main() {
    log.info("=== Starting FatSecret API ===")

    val config = HoconApplicationConfig(ConfigFactory.load())

    // Read port from config
    val port = config.propertyOrNull("ktor.deployment.port")?.getString()?.toInt()
        ?: throw IllegalStateException("ktor.deployment.port not found in configuration")

    // Read client credentials from config
    val clientId = config.propertyOrNull("fatsecret.clientId")?.getString()
        ?: throw IllegalStateException("fatsecret.clientId not found in configuration")

    val clientSecret = config.propertyOrNull("fatsecret.clientSecret")?.getString()
        ?: throw IllegalStateException("fatsecret.clientSecret not found in configuration")

    log.info("Server configuration loaded")
    log.info("Port: {}", port)
    log.info("Host: 0.0.0.0")
    log.info("FatSecret Client ID: {}...", clientId.take(8))

    val fatSecretService = try {
        FatSecretService(clientId, clientSecret).also {
            log.info("FatSecretService initialized successfully")
        }
    } catch (e: Exception) {
        log.error("Failed to initialize FatSecretService: {}", e.message, e)
        throw e
    }

    embeddedServer(Netty, port = port, host = "0.0.0.0", module = {
        mainModule(fatSecretService)
    }).start(wait = true)
}

fun Application.mainModule(fatSecretService: FatSecretService) {
    // Get logger for the application module
    val appLog = LoggerFactory.getLogger("Application")

    monitor.subscribe(ApplicationStarted) {
        appLog.info("Application started successfully")
    }

    appLog.info("Configuring Ktor application module")

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
        appLog.info("ContentNegotiation plugin installed")
    }

    install(CORS) {
        anyHost()
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        appLog.info("CORS plugin installed")
    }

    install(DefaultHeaders) {
        appLog.info("DefaultHeaders plugin installed")
    }

    install(CallLogging) {
        level = Level.INFO
        appLog.info("CallLogging plugin installed (level: INFO)")
    }

    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            appLog.warn("Resource not found: {}", call.request.uri)
            call.respond(
                status,
                ApiError(
                    code = status.value,
                    message = "Resource not found"
                )
            )
        }

        exception<Throwable> { call, cause ->
            appLog.error("Unhandled exception: {}", cause.message, cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ApiError(
                    code = 500,
                    message = "Internal server error",
                    details = cause.message
                )
            )
        }
        appLog.info("StatusPages plugin installed")
    }

    routing {
        get("/") {
            appLog.info("Root endpoint accessed")
            call.respondText("FatSecret API is running. Use /search/v1 or /food/{id} endpoints.")
        }

        get("/health") {
            appLog.debug("Health check endpoint accessed")
            call.respond(mapOf("status" to "ok", "service" to "fatsecret-api"))
        }

        // v1 API поиск (использует OAuth 2.0 токен)
        get("/search/v1") {
            val searchExpression =
                call.request.queryParameters["query"]

            if (searchExpression.isNullOrEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiError(
                        code = 101,
                        message = "Missing required parameter: query "
                    )
                )
                return@get
            }

            val maxResults = call.request.queryParameters["max_results"]?.toIntOrNull() ?: 20
            val pageNumber = call.request.queryParameters["page"]?.toIntOrNull()
                ?: call.request.queryParameters["page_number"]?.toIntOrNull()
                ?: 0
            val format = call.request.queryParameters["format"] ?: "json"
            val genericDescription = call.request.queryParameters["generic_description"]
            val region = call.request.queryParameters["region"] ?: "US"
            val language = call.request.queryParameters["language"]

            // Валидация параметров
            if (maxResults > 50) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiError(
                        code = 107,
                        message = "max_results cannot be greater than 50"
                    )
                )
                return@get
            }

            if (maxResults < 1) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiError(
                        code = 107,
                        message = "max_results must be at least 1"
                    )
                )
                return@get
            }

            println("v1 Search request: query='$searchExpression', maxResults=$maxResults, page=$pageNumber")

            try {
                val result = fatSecretService.searchFoodsV1(
                    searchExpression = searchExpression,
                    maxResults = maxResults,
                    pageNumber = pageNumber,
                    format = format,
                    genericDescription = genericDescription,
                    region = region,
                    language = language
                )

                result.onSuccess { response ->
                    call.respond(response)
                }.onFailure { error ->
                    val errorMessage = error.message ?: "Unknown error"

                    when {
                        errorMessage.contains("error 13", ignoreCase = true) -> {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                ApiError(
                                    code = 13,
                                    message = "Invalid token",
                                    details = errorMessage
                                )
                            )
                        }

                        errorMessage.contains("error 14", ignoreCase = true) -> {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                ApiError(
                                    code = 14,
                                    message = "Missing scope",
                                    details = errorMessage
                                )
                            )
                        }

                        errorMessage.contains("error 101", ignoreCase = true) -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ApiError(
                                    code = 101,
                                    message = "Missing required parameter",
                                    details = errorMessage
                                )
                            )
                        }

                        errorMessage.contains("error 107", ignoreCase = true) -> {
                            call.respond(
                                HttpStatusCode.BadRequest,
                                ApiError(
                                    code = 107,
                                    message = "Value out of range",
                                    details = errorMessage
                                )
                            )
                        }

                        else -> {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ApiError(
                                    code = 500,
                                    message = "Failed to search food",
                                    details = errorMessage
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error searching food v1: ${e.message}")
                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiError(
                        code = 500,
                        message = "Failed to search food",
                        details = e.message
                    )
                )
            }
        }

        // Получение детальной информации о продукте
        get("/food/{id}") {
            val foodId = call.parameters["id"]

            if (foodId.isNullOrEmpty()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ApiError(
                        code = 101,
                        message = "Missing required parameter: food_id"
                    )
                )
                return@get
            }

            val format = call.request.queryParameters["format"] ?: "json"

            try {
                val result = fatSecretService.getFoodDetails(
                    foodId = foodId,
                    format = format
                )

                result.onSuccess { response ->
                    call.respond(response)
                }.onFailure { error ->
                    val errorMessage = error.message ?: "Unknown error"

                    when {
                        errorMessage.contains("error 13", ignoreCase = true) -> {
                            call.respond(
                                HttpStatusCode.Unauthorized,
                                ApiError(
                                    code = 13,
                                    message = "Invalid token",
                                    details = errorMessage
                                )
                            )
                        }

                        errorMessage.contains("error 14", ignoreCase = true) -> {
                            call.respond(
                                HttpStatusCode.Forbidden,
                                ApiError(
                                    code = 14,
                                    message = "Missing scope",
                                    details = errorMessage
                                )
                            )
                        }

                        else -> {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                ApiError(
                                    code = 500,
                                    message = "Failed to get food details",
                                    details = errorMessage
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error getting food details: ${e.message}")
                e.printStackTrace()

                call.respond(
                    HttpStatusCode.InternalServerError,
                    ApiError(
                        code = 500,
                        message = "Failed to get food details",
                        details = e.message
                    )
                )
            }
        }

        get("/info") {
            appLog.info("Info endpoint accessed")

            val response = ApiInfo(
                service = "FatSecret API",
                version = "1.0.0",
                description = "API для работы с FatSecret (поиск продуктов, детальная информация, штрих-коды)",
                authentication = "OAuth 2.0 with basic scope",
                endpoints = listOf(
                    EndpointInfo("GET", "/search/v1", "Поиск продуктов по названию"),
                    EndpointInfo("GET", "/food/{id}", "Получение детальной информации о продукте"),
                    EndpointInfo("GET", "/health", "Проверка работоспособности"),
                    EndpointInfo("GET", "/info", "Информация о сервисе")
                ),
                searchParameters = listOf(
                    "query - название продукта для поиска (обязательно)",
                    "max_results - максимальное количество результатов (1-50, по умолчанию: 20)",
                    "page или page_number - номер страницы (по умолчанию: 0)",
                    "generic_description - 'weight' или 'portion' (только premier)"
                )
            )

            call.respond(response)
        }

        monitor.subscribe(ApplicationStopping) {
            appLog.info("Application is stopping")
            fatSecretService.close()
            appLog.info("FatSecretService resources released")
        }
    }
}
