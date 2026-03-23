package com.nutrilogic.recommendation.mapper

import com.nutrilogic.recommendation.dto.UserDto
import com.nutrilogic.recommendation.entity.User
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toDto(user: User): UserDto {
        return UserDto(
            id = user.id,
            username = user.username,
            role = user.role,
            name = user.name,
            gender = user.gender,
            height = user.height,
            weight = user.weight,
            targetWeight = user.targetWeight,
            activityLevel = user.activityLevel
        )
    }

    fun toEntity(dto: UserDto, password: String): User {
        return User(
            username = dto.username,
            password = password,
            role = dto.role,
            name = dto.name,
            gender = dto.gender,
            height = dto.height,
            weight = dto.weight,
            targetWeight = dto.targetWeight,
            activityLevel = dto.activityLevel
        )
    }

    fun toDtoList(users: List<User>): List<UserDto> = users.map { toDto(it) }
}