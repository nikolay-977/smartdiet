package com.nutrilogic.recommendation.entity

import jakarta.persistence.*

@Entity
@Table(name = "consumed_products")
data class ConsumedProduct(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "diary_entry_id", nullable = false)
    var diaryEntry: DiaryEntry?,

    @Column(nullable = false)
    var productId: Long,

    @Column(nullable = false)
    var quantity: Double,

    var name: String? = null,
    var calories: Double? = null,
    var protein: Double? = null,
    var fat: Double? = null,
    var carbs: Double? = null
)