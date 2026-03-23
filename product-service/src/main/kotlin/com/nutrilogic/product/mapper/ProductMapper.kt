package com.nutrilogic.product.mapper

import com.nutrilogic.product.dto.ProductDto
import com.nutrilogic.product.entity.Product
import org.mapstruct.*

@Mapper(componentModel = "spring")
abstract class ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    abstract fun toEntity(dto: ProductDto): Product

    abstract fun toDto(product: Product): ProductDto

    fun toDtoList(products: List<Product>): List<ProductDto> = products.map { toDto(it) }
}