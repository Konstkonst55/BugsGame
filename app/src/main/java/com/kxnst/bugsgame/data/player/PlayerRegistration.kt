package com.kxnst.bugsgame.data.player

import kotlinx.serialization.Serializable

@Serializable
data class PlayerRegistration(
    val fullName: String,
    val gender: String,
    val course: String,
    val difficulty: Int,
    val birthDate: String,
    val zodiacName: String
)
