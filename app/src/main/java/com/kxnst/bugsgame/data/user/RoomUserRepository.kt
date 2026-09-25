package com.kxnst.bugsgame.data.user

import com.kxnst.bugsgame.data.database.UserDao
import com.kxnst.bugsgame.data.database.toEntity
import com.kxnst.bugsgame.domain.user.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomUserRepository(
    private val userDao: UserDao
) : UserRepository {
    override fun observeUsers(): Flow<List<UserProfile>> {
        return userDao.observeUsers().map { users ->
            users.map { it.toDomain() }
        }
    }

    override suspend fun register(user: UserProfile): UserRegistrationResult {
        val insertedId = userDao.insertUser(user.toEntity())

        return if (insertedId == INSERT_FAILED) {
            UserRegistrationResult.DUPLICATE_NAME
        } else {
            UserRegistrationResult.SUCCESS
        }
    }

    override suspend fun getUser(name: String): UserProfile? {
        return userDao.getUser(name)?.toDomain()
    }

    override suspend fun updateBestScore(name: String, score: Int) {
        userDao.updateBestScoreIfHigher(name, score)
    }

    private companion object {
        const val INSERT_FAILED = -1L
    }
}
