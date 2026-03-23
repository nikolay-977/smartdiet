package com.nutrilogic.recommendation.service

import com.nutrilogic.recommendation.dto.AuthRequest
import com.nutrilogic.recommendation.dto.AuthResponse
import com.nutrilogic.recommendation.entity.Role
import com.nutrilogic.recommendation.exception.UserNotFoundException
import com.nutrilogic.recommendation.mapper.UserMapper
import com.nutrilogic.recommendation.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val userRepository: UserRepository,
    private val userService: UserService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper
) {

    fun login(request: AuthRequest): AuthResponse {
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { UserNotFoundException() }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw UserNotFoundException("Invalid credentials")
        }

        val token = jwtService.generateToken(user.id!!)
        val userDto = userMapper.toDto(user)

        return AuthResponse(token, userDto)
    }

    fun register(username: String, password: String, role: Role = Role.USER): AuthResponse {
        val userDto = userService.register(username, password, role)
        val token = jwtService.generateToken(userDto.id!!)

        return AuthResponse(token, userDto)
    }
}