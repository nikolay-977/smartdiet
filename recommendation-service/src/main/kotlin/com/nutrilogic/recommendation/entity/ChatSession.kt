package com.nutrilogic.recommendation.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "chat_sessions")
data class ChatSession(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    var createdAt: LocalDateTime = LocalDateTime.now()
)