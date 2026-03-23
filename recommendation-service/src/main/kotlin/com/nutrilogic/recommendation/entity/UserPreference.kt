package com.nutrilogic.recommendation.entity

import jakarta.persistence.*

@Entity
@Table(name = "user_preferences")
data class UserPreference(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var productId: Long,

    @Enumerated(EnumType.STRING)
    var preferenceType: PreferenceType
)

