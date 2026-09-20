package com.kxnst.bugsgame.data.settings

data class GameSettings(
    val speed: Int,
    val maxCockroaches: Int,
    val bonusIntervalSeconds: Int,
    val roundDurationSeconds: Int
)

object GameSettingConstraints {
    const val minSpeed = 1
    const val maxSpeed = 10
    const val defaultSpeed = 5

    const val minMaxCockroaches = 1
    const val maxMaxCockroaches = 10
    const val defaultMaxCockroaches = 3

    const val minBonusIntervalSeconds = 5
    const val maxBonusIntervalSeconds = 60
    const val defaultBonusIntervalSeconds = 15

    const val minRoundDurationSeconds = 30
    const val maxRoundDurationSeconds = 300
    const val defaultRoundDurationSeconds = 60
}
