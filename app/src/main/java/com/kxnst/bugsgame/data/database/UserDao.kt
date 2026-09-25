package com.kxnst.bugsgame.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users ORDER BY bestScore DESC, name COLLATE NOCASE ASC")
    fun observeUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE name = :name LIMIT 1")
    suspend fun getUser(name: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("UPDATE users SET bestScore = :score WHERE name = :name AND bestScore < :score")
    suspend fun updateBestScoreIfHigher(name: String, score: Int): Int
}
