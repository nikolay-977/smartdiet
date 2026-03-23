package com.nutrilogic.recommendation.entity

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.LocalDate

@Entity
@Table(name = "users")
class User : UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(unique = true, nullable = false)
    private var username: String = ""

    @Column(nullable = false)
    private var password: String = ""

    @Enumerated(EnumType.STRING)
    var role: Role = Role.USER

    var name: String? = null
    var gender: String? = null
    var birthDate: LocalDate? = null
    var height: Double? = null
    var weight: Double? = null
    var targetWeight: Double? = null
    var activityLevel: String? = null

    constructor() {}

    constructor(
        username: String,
        password: String,
        role: Role = Role.USER,
        name: String? = null,
        gender: String? = null,
        birthDate: LocalDate? = null,
        height: Double? = null,
        weight: Double? = null,
        targetWeight: Double? = null,
        activityLevel: String? = null
    ) {
        this.username = username
        this.password = password
        this.role = role
        this.name = name
        this.gender = gender
        this.birthDate = birthDate
        this.height = height
        this.weight = weight
        this.targetWeight = targetWeight
        this.activityLevel = activityLevel
    }

    fun getUsernameField(): String = username
    fun setUsernameField(username: String) { this.username = username }

    fun getPasswordField(): String = password
    fun setPasswordField(password: String) { this.password = password }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> =
        mutableListOf(SimpleGrantedAuthority("ROLE_${role.name}"))

    override fun getPassword(): String = password
    override fun getUsername(): String = username
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}