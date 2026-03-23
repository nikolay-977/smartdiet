package com.nutrilogic.product.controller

import com.nutrilogic.product.dto.ProductDto
import com.nutrilogic.product.service.ProductService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Product management endpoints")
class ProductController(
    private val productService: ProductService
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new product")
    fun createProduct(@RequestBody @Valid productDto: ProductDto): ResponseEntity<ProductDto> {
        return ResponseEntity.ok(productService.createProduct(productDto))
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get all products with pagination")
    fun getAllProducts(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "100") size: Int
    ): ResponseEntity<List<ProductDto>> {
        return ResponseEntity.ok(productService.getAllProducts(page, size))
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get product by ID")
    fun getProductById(@PathVariable id: Long): ResponseEntity<ProductDto> {
        return ResponseEntity.ok(productService.getProductById(id))
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update product")
    fun updateProduct(
        @PathVariable id: Long,
        @RequestBody @Valid productDto: ProductDto
    ): ResponseEntity<ProductDto> {
        return ResponseEntity.ok(productService.updateProduct(id, productDto))
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete product")
    fun deleteProduct(@PathVariable id: Long): ResponseEntity<Void> {
        productService.deleteProduct(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Search products by name")
    fun searchByName(
        @RequestParam name: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "100") size: Int
    ): ResponseEntity<List<ProductDto>> {
        return ResponseEntity.ok(productService.searchByName(name, page, size))
    }

    @GetMapping("/recommend")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get recommended products by nutrient efficiency")
    fun getRecommendedProducts(
        @RequestParam nutrient: String,
        @RequestParam(defaultValue = "7") limit: Int
    ): ResponseEntity<List<ProductDto>> {
        return ResponseEntity.ok(productService.getRecommendedProductsByNutrient(nutrient, limit))
    }

    @PostMapping("/batch")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get multiple products by IDs")
    fun getProductsByIds(@RequestBody ids: List<Long>): ResponseEntity<List<ProductDto>> {
        return ResponseEntity.ok(productService.getProductsByIds(ids))
    }
}