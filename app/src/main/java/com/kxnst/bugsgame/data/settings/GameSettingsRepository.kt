package com.kxnst.bugsgame.data.settings

import kotlinx.coroutines.flow.StateFlow

interface GameSettingsRepository {
    val settings: StateFlow<GameSettings>

    fun updateSpeed(value: Int)

    fun updateMaxCockroaches(value: Int)

    fun updateBonusIntervalSeconds(value: Int)

    fun updateRoundDurationSeconds(value: Int)
}
