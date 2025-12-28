package ru.smartdiet.products.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import ru.smartdiet.products.dto.UsdaSearchResponse

@FeignClient(
    name = "usda",
    url = "https://api.nal.usda.gov/fdc/v1",
    configuration = [UsdaClientConfiguration::class]
)
interface UsdaClient {

    @GetMapping("/foods/search")
    fun searchFoods(
        @RequestParam("api_key") apiKey: String,
        @RequestParam query: String,
        @RequestParam("pageSize") pageSize: Int = 25,
        @RequestParam("pageNumber") pageNumber: Int = 1,
        @RequestParam("dataType") dataType: String = "Foundation"
    ): UsdaSearchResponse
}

class UsdaClientConfiguration {
}