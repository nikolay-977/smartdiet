package ru.smartdiet.products.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.smartdiet.products.dto.ProductResponse
import ru.smartdiet.products.service.ProductService
import ru.smartdiet.products.service.UsdaService

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
    private val usdaService: UsdaService
) {

    @GetMapping("/{barcode}")
    fun getProduct(@PathVariable barcode: String): ResponseEntity<ProductResponse> {
        val productInfo = productService.getRussianProductInfo(barcode)
        return ResponseEntity.ok(productInfo)
    }

    @GetMapping("/search/usda")
    fun searchUsdaProducts(@RequestParam query: String): ResponseEntity<List<ProductResponse>> {
        val products = usdaService.searchProducts(query)
        return ResponseEntity.ok(products)
    }
}