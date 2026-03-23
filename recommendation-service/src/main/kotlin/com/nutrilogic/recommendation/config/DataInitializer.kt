package com.nutrilogic.recommendation.config

import com.nutrilogic.recommendation.entity.Role
import com.nutrilogic.recommendation.entity.User
import com.nutrilogic.recommendation.repository.UserRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        if (userRepository.findByUsername("admin").isEmpty) {
            val admin = User(
                username = "admin",
                password = passwordEncoder.encode("admin123"),
                role = Role.ADMIN,
                name = "Administrator"
            )
            userRepository.save(admin)
            println("Admin user created: admin/admin123")
        }

        if (userRepository.findByUsername("user").isEmpty) {
            val user = User(
                username = "user",
                password = passwordEncoder.encode("user123"),
                role = Role.USER,
                name = "Test User"
            )
            userRepository.save(user)
            println("Test user created: user/user123")
        }

        println("Data initialization completed. Total users: ${userRepository.count()}")
    }
}