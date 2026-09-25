package com.kxnst.bugsgame.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kxnst.bugsgame.domain.user.UserProfile

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val name: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val birthDate: String,
    val zodiacName: String,
    val bestScore: Int
) {
    fun toDomain(): UserProfile {
        return UserProfile(
            name = name,
            gender = gender,
            course = course,
            difficulty = difficulty,
            birthDate = birthDate,
            zodiacName = zodiacName,
            bestScore = bestScore
        )
    }
}

fun UserProfile.toEntity(): UserEntity {
    return UserEntity(
        name = name,
        gender = gender,
        course = course,
        difficulty = difficulty,
        birthDate = birthDate,
        zodiacName = zodiacName,
        bestScore = bestScore
    )
}
