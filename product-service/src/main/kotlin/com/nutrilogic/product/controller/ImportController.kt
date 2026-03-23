package com.nutrilogic.product.controller

import com.nutrilogic.product.dto.ImportResponse
import com.nutrilogic.product.service.ImportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/admin/import")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Import", description = "Data import endpoints")
class ImportController(
    private val importService: ImportService
) {

    @PostMapping("/food-data")
    @Operation(summary = "Import products from FoodData Central JSON file")
    fun importFoodData(
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ImportResponse> {
        // Проверка размера файла
        if (file.size > 100 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(
                ImportResponse(
                    success = false,
                    message = "File size exceeds 100 MB limit",
                    importedCount = 0,
                    totalCount = 0,
                    error = "File too large"
                )
            )
        }

        val result = importService.importFoodDataFromJson(file)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/status")
    @Operation(summary = "Get last import status")
    fun getImportStatus(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to importService.getLastImportStatus(),
                "totalProducts" to importService.getTotalProductsCount()
            )
        )
    }
}