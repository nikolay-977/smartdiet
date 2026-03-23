package com.nutrilogic.recommendation.entity

import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "diary_entries")
data class DiaryEntry(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var entryDate: LocalDate = LocalDate.now(),

    @OneToMany(mappedBy = "diaryEntry", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var consumedProducts: MutableList<ConsumedProduct> = mutableListOf(),

    var totalCalories: Double = 0.0,
    var totalProtein: Double = 0.0,
    var totalFat: Double = 0.0,
    var totalCarbs: Double = 0.0
)