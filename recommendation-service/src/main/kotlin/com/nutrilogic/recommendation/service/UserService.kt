package com.nutrilogic.recommendation.service

import com.nutrilogic.recommendation.dto.UserDto
import com.nutrilogic.recommendation.entity.Role
import com.nutrilogic.recommendation.entity.User
import com.nutrilogic.recommendation.exception.UserNotFoundException
import com.nutrilogic.recommendation.exception.UsernameExistsException
import com.nutrilogic.recommendation.mapper.UserMapper
import com.nutrilogic.recommendation.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper
) {

    fun register(username: String, rawPassword: String, role: Role = Role.USER): UserDto {
        if (userRepository.existsByUsername(username)) {
            throw UsernameExistsException()
        }

        val user = User(
            username = username,
            password = passwordEncoder.encode(rawPassword),
            role = role
        )

        val saved = userRepository.save(user)
        return userMapper.toDto(saved)
    }

    fun updateProfile(userId: Long, userDto: UserDto): UserDto {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        user.name = userDto.name
        user.gender = userDto.gender
        user.height = userDto.height
        user.weight = userDto.weight
        user.targetWeight = userDto.targetWeight
        user.activityLevel = userDto.activityLevel

        val updated = userRepository.save(user)
        return userMapper.toDto(updated)
    }

    fun getUserProfile(userId: Long): UserDto {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        return userMapper.toDto(user)
    }

    fun deleteUser(userId: Long) {
        if (!userRepository.existsById(userId)) {
            throw UserNotFoundException()
        }
        userRepository.deleteById(userId)
    }
}