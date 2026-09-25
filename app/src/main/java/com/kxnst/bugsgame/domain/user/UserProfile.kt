package com.kxnst.bugsgame.domain.user

data class UserProfile(
    val name: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val birthDate: String,
    val zodiacName: String,
    val bestScore: Int
)
