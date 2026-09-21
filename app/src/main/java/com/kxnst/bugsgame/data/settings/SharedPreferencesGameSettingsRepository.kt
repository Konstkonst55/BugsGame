package com.kxnst.bugsgame.data.settings

import android.content.Context

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.core.content.edit

class SharedPreferencesGameSettingsRepository(
    context: Context
) : GameSettingsRepository {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    private val _settings = MutableStateFlow(readSettings())
    override val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    override fun updateSpeed(value: Int) {
        val safeValue = value.coerceIn(
            GameSettingConstraints.MIN_SPEED,
            GameSettingConstraints.MAX_SPEED
        )

        preferences.edit { putInt(KEY_SPEED, safeValue) }
        updateState { it.copy(speed = safeValue) }
    }

    override fun updateMaxCockroaches(value: Int) {
        val safeValue = value.coerceIn(
            GameSettingConstraints.MIN_MAX_COCKROACHES,
            GameSettingConstraints.MAX_MAX_COCKROACHES
        )

        preferences.edit { putInt(KEY_MAX_COCKROACHES, safeValue) }
        updateState { it.copy(maxCockroaches = safeValue) }
    }

    override fun updateBonusIntervalSeconds(value: Int) {
        val safeValue = value.coerceIn(
            GameSettingConstraints.MIN_BONUS_INTERVAL_SECONDS,
            GameSettingConstraints.MAX_BONUS_INTERVAL_SECONDS
        )

        preferences.edit { putInt(KEY_BONUS_INTERVAL_SECONDS, safeValue) }
        updateState { it.copy(bonusIntervalSeconds = safeValue) }
    }

    override fun updateRoundDurationSeconds(value: Int) {
        val safeValue = value.coerceIn(
            GameSettingConstraints.MIN_ROUND_DURATION_SECONDS,
            GameSettingConstraints.MAX_ROUND_DURATION_SECONDS
        )

        preferences.edit { putInt(KEY_ROUND_DURATION_SECONDS, safeValue) }
        updateState { it.copy(roundDurationSeconds = safeValue) }
    }

    private fun readSettings(): GameSettings {
        return GameSettings(
            speed = preferences.getInt(
                KEY_SPEED,
                GameSettingConstraints.DEFAULT_SPEED
            ).coerceIn(
                GameSettingConstraints.MIN_SPEED,
                GameSettingConstraints.MAX_SPEED
            ),
            maxCockroaches = preferences.getInt(
                KEY_MAX_COCKROACHES,
                GameSettingConstraints.DEFAULT_MAX_COCKROACHES
            ).coerceIn(
                GameSettingConstraints.MIN_MAX_COCKROACHES,
                GameSettingConstraints.MAX_MAX_COCKROACHES
            ),
            bonusIntervalSeconds = preferences.getInt(
                KEY_BONUS_INTERVAL_SECONDS,
                GameSettingConstraints.DEFAULT_BONUS_INTERVAL_SECONDS
            ).coerceIn(
                GameSettingConstraints.MIN_BONUS_INTERVAL_SECONDS,
                GameSettingConstraints.MAX_BONUS_INTERVAL_SECONDS
            ),
            roundDurationSeconds = preferences.getInt(
                KEY_ROUND_DURATION_SECONDS,
                GameSettingConstraints.DEFAULT_ROUND_DURATION_SECONDS
            ).coerceIn(
                GameSettingConstraints.MIN_ROUND_DURATION_SECONDS,
                GameSettingConstraints.MAX_ROUND_DURATION_SECONDS
            )
        )
    }

    private fun updateState(transform: (GameSettings) -> GameSettings) {
        _settings.value = transform(_settings.value)
    }

    private companion object {
        const val PREFERENCES_NAME = "game_settings_preferences"
        const val KEY_SPEED = "speed"
        const val KEY_MAX_COCKROACHES = "max_cockroaches"
        const val KEY_BONUS_INTERVAL_SECONDS = "bonus_interval_seconds"
        const val KEY_ROUND_DURATION_SECONDS = "round_duration_seconds"
    }
}
