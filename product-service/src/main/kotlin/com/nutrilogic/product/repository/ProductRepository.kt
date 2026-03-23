package com.nutrilogic.product.repository

import com.nutrilogic.product.entity.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface ProductRepository : JpaRepository<Product, Long> {

    fun findByNameContainingIgnoreCase(name: String, pageable: Pageable): Page<Product>

    @Query("SELECT p FROM Product p WHERE p.category = :category")
    fun findByCategory(@Param("category") category: String, pageable: Pageable): Page<Product>

    @Query("SELECT p FROM Product p WHERE p.calories BETWEEN :minCalories AND :maxCalories")
    fun findByCaloriesRange(
        @Param("minCalories") minCalories: Double,
        @Param("maxCalories") maxCalories: Double,
        pageable: Pageable
    ): Page<Product>

    @Query("SELECT COUNT(p) FROM Product p")
    fun countAll(): Long
}