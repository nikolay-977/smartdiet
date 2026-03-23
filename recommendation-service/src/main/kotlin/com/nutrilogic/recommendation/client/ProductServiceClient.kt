package com.nutrilogic.recommendation.client

import com.nutrilogic.recommendation.config.FeignConfig
import com.nutrilogic.recommendation.dto.ProductDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.*

@FeignClient(
    name = "product-service",
    url = "\${product-service.url:http://localhost:8082}",
    configuration = [FeignConfig::class]
)
interface ProductServiceClient {

    @GetMapping("/api/products/{id}")
    fun getProduct(@PathVariable("id") id: Long): ProductDto

    @GetMapping("/api/products/barcode/{barcode}")
    fun findByBarcode(@PathVariable("barcode") barcode: String): ProductDto?

    @GetMapping("/api/products/search")
    fun searchByName(
        @RequestParam("name") name: String,
        @RequestParam("page") page: Int = 0,
        @RequestParam("size") size: Int = 100
    ): List<ProductDto>

    @GetMapping("/api/products/recommend")
    fun getRecommendedProducts(
        @RequestParam("nutrient") nutrient: String,
        @RequestParam("limit") limit: Int = 7
    ): List<ProductDto>

    @GetMapping("/api/products")
    fun getAllProducts(): List<ProductDto>

    @PostMapping("/api/products/batch")
    fun getProductsByIds(@RequestBody ids: List<Long>): List<ProductDto>
}