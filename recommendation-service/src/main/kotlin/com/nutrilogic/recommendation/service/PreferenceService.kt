package com.nutrilogic.recommendation.service

import com.nutrilogic.recommendation.entity.PreferenceType
import com.nutrilogic.recommendation.entity.UserPreference
import com.nutrilogic.recommendation.exception.UserNotFoundException
import com.nutrilogic.recommendation.repository.UserPreferenceRepository
import com.nutrilogic.recommendation.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PreferenceService(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val userRepository: UserRepository
) {

    fun addToFavorites(userId: Long, productId: Long) {
        addPreference(userId, productId, PreferenceType.FAVORITE)
    }

    fun removeFromFavorites(userId: Long, productId: Long) {
        removePreference(userId, productId, PreferenceType.FAVORITE)
    }

    fun addToStopList(userId: Long, productId: Long) {
        addPreference(userId, productId, PreferenceType.BANNED)
    }

    fun removeFromStopList(userId: Long, productId: Long) {
        removePreference(userId, productId, PreferenceType.BANNED)
    }

    private fun addPreference(userId: Long, productId: Long, type: PreferenceType) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }

        if (!userPreferenceRepository.existsByUserIdAndProductIdAndPreferenceType(userId, productId, type)) {
            val preference = UserPreference(
                user = user,
                productId = productId,
                preferenceType = type
            )
            userPreferenceRepository.save(preference)
        }
    }

    private fun removePreference(userId: Long, productId: Long, type: PreferenceType) {
        userPreferenceRepository.findByUserIdAndProductId(userId, productId)
            .filter { it.preferenceType == type }
            .ifPresent { userPreferenceRepository.delete(it) }
    }

    fun getFavorites(userId: Long): List<Long> {
        return userPreferenceRepository.findFavoriteProductIds(userId)
    }

    fun getStopList(userId: Long): List<Long> {
        return userPreferenceRepository.findBannedProductIds(userId)
    }
}