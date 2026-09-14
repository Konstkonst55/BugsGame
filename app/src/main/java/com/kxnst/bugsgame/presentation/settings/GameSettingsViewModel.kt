package com.kxnst.bugsgame.presentation.settings

import androidx.lifecycle.ViewModel
import com.kxnst.bugsgame.data.settings.GameSettings
import com.kxnst.bugsgame.data.settings.GameSettingsRepository
import kotlinx.coroutines.flow.StateFlow

class GameSettingsViewModel(
    private val repository: GameSettingsRepository
) : ViewModel() {
    val settings: StateFlow<GameSettings> = repository.settings

    fun updateSpeed(value: Int) {
        repository.updateSpeed(value)
    }

    fun updateMaxCockroaches(value: Int) {
        repository.updateMaxCockroaches(value)
    }

    fun updateBonusIntervalSeconds(value: Int) {
        repository.updateBonusIntervalSeconds(value)
    }

    fun updateRoundDurationSeconds(value: Int) {
        repository.updateRoundDurationSeconds(value)
    }
}
