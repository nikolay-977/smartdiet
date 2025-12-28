package ru.smartdiet.products.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.smartdiet.products.dto.ProductResponse
import ru.smartdiet.products.service.ProductService

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService
) {

    @GetMapping("/{barcode}")
    fun getProductByBarcode(@PathVariable barcode: String): ResponseEntity<ProductResponse> {
        val productInfo = productService.getProductInfo(barcode)
        return ResponseEntity.ok(productInfo)
    }

    @GetMapping("/test")
    fun test(): String {
        return "Products Service is running!"
    }

    @GetMapping("/health")
    fun health(): Map<String, String> {
        return mapOf("status" to "UP")
    }
}