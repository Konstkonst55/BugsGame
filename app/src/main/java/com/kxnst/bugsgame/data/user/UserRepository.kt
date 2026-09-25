package com.kxnst.bugsgame.data.user

import com.kxnst.bugsgame.domain.user.UserProfile
import kotlinx.coroutines.flow.Flow

enum class UserRegistrationResult {
    SUCCESS,
    DUPLICATE_NAME
}

interface UserRepository {
    fun observeUsers(): Flow<List<UserProfile>>

    suspend fun register(user: UserProfile): UserRegistrationResult

    suspend fun getUser(name: String): UserProfile?

    suspend fun updateBestScore(name: String, score: Int)
}
