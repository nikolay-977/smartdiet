package com.nutrilogic.recommendation.service

import com.nutrilogic.recommendation.dto.UserDto
import com.nutrilogic.recommendation.mapper.UserMapper
import com.nutrilogic.recommendation.repository.UserRepository
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey

@Service
class JwtService(
    @Value("\${jwt.secret}") private val jwtSecret: String,
    @Value("\${jwt.expiration:3600000}") private val expiration: Long,
    private val userRepository: UserRepository,
    private val userMapper: UserMapper
) {
    private val secretKey: SecretKey by lazy {
        val keyBytes = Base64.getDecoder().decode(jwtSecret)
        Keys.hmacShaKeyFor(keyBytes)
    }

    fun generateToken(userId: Long): String {
        val user = userRepository.findById(userId).orElseThrow()
        return Jwts.builder()
            .setSubject(user.username)
            .claim("role", user.role.name)
            .claim("userId", user.id)
            .setIssuedAt(Date())
            .setExpiration(Date(System.currentTimeMillis() + expiration))
            .signWith(secretKey)
            .compact()
    }

    fun extractUser(token: String): UserDto? {
        return try {
            val claims = parseClaims(token)
            val userId = claims["userId"] as? Int ?: return null
            userRepository.findById(userId.toLong())
                .map(userMapper::toDto)
                .orElse(null)
        } catch (e: Exception) {
            null
        }
    }

    fun validateToken(token: String): Claims? {
        return try {
            parseClaims(token)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .body
    }
}