package com.nutrilogic.recommendation.repository

import com.nutrilogic.recommendation.entity.PreferenceType
import com.nutrilogic.recommendation.entity.UserPreference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserPreferenceRepository : JpaRepository<UserPreference, Long> {

    fun findByUserIdAndProductId(userId: Long, productId: Long): Optional<UserPreference>

    fun findAllByUserIdAndPreferenceType(userId: Long, preferenceType: PreferenceType): List<UserPreference>

    @Query("SELECT up.productId FROM UserPreference up WHERE up.user.id = :userId AND up.preferenceType = :type")
    fun findProductIdsByUserIdAndType(@Param("userId") userId: Long, @Param("type") type: PreferenceType): List<Long>

    @Query("SELECT up.productId FROM UserPreference up WHERE up.user.id = :userId AND up.preferenceType = 'FAVORITE'")
    fun findFavoriteProductIds(@Param("userId") userId: Long): List<Long>

    @Query("SELECT up.productId FROM UserPreference up WHERE up.user.id = :userId AND up.preferenceType = 'BANNED'")
    fun findBannedProductIds(@Param("userId") userId: Long): List<Long>

    fun existsByUserIdAndProductIdAndPreferenceType(userId: Long, productId: Long, preferenceType: PreferenceType): Boolean
}