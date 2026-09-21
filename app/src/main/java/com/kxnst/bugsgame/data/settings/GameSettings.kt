package com.kxnst.bugsgame.data.settings

data class GameSettings(
    val speed: Int,
    val maxCockroaches: Int,
    val bonusIntervalSeconds: Int,
    val roundDurationSeconds: Int
)

object GameSettingConstraints {
    const val MIN_SPEED = 1
    const val MAX_SPEED = 10
    const val DEFAULT_SPEED = 5

    const val MIN_MAX_COCKROACHES = 1
    const val MAX_MAX_COCKROACHES = 10
    const val DEFAULT_MAX_COCKROACHES = 3

    const val MIN_BONUS_INTERVAL_SECONDS = 5
    const val MAX_BONUS_INTERVAL_SECONDS = 60
    const val DEFAULT_BONUS_INTERVAL_SECONDS = 15

    const val MIN_ROUND_DURATION_SECONDS = 30
    const val MAX_ROUND_DURATION_SECONDS = 300
    const val DEFAULT_ROUND_DURATION_SECONDS = 60
}
