package com.kxnst.bugsgame.data.settings

data class GameSettings(
    val speed: Int,
    val maxCockroaches: Int,
    val bonusIntervalSeconds: Int,
    val roundDurationSeconds: Int
)
