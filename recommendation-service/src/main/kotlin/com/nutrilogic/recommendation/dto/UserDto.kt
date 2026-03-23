package com.nutrilogic.recommendation.dto

import com.nutrilogic.recommendation.entity.Role
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority

data class UserDto(
    val id: Long?,
    val username: String,
    val role: Role,
    val name: String? = null,
    val gender: String? = null,
    val height: Double? = null,
    val weight: Double? = null,
    val targetWeight: Double? = null,
    val activityLevel: String? = null
) {
    fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
}