package ru.smartdiet.products.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(
    name = "openfoodfacts",
    url = "https://world.openfoodfacts.org"
)
interface OpenFoodFactsClient {

    @GetMapping("/api/v0/product/{barcode}.json")
    fun getProduct(@PathVariable barcode: String): Map<String, Any>
}