package com.nutrilogic.product.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.annotations.UpdateTimestamp
import org.hibernate.type.SqlTypes
import java.time.LocalDateTime

@Entity
@Table(name = "products")
data class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var calories: Double = 0.0,

    @Column(nullable = false)
    var protein: Double = 0.0,

    @Column(nullable = false)
    var fat: Double = 0.0,

    @Column(nullable = false)
    var carbs: Double = 0.0,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var vitamins: Map<String, Double>? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var minerals: Map<String, Double>? = null,

    var category: String? = null,

    var manufacturer: String? = null,

    var servingSize: Double = 100.0,

    var servingUnit: String = "g",

    @CreationTimestamp
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @UpdateTimestamp
    var updatedAt: LocalDateTime = LocalDateTime.now()
)