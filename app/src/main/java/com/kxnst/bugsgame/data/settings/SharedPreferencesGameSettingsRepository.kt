package com.kxnst.bugsgame.data.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedPreferencesGameSettingsRepository(
    context: Context
) : GameSettingsRepository {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(readSettings())
    override val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    override fun updateSpeed(value: Int) {
        val safeValue = value.coerceIn(GameSettingConstraints.minSpeed, GameSettingConstraints.maxSpeed)
        preferences.edit().putInt(KEY_SPEED, safeValue).apply()
        updateState { it.copy(speed = safeValue) }
    }

    override fun updateMaxCockroaches(value: Int) {
        val safeValue = value.coerceIn(
            GameSettingConstraints.minMaxCockroaches,
            GameSettingConstraints.maxMaxCockroaches
        )
        preferences.edit().putInt(KEY_MAX_COCKROACHES, safeValue).apply()
        updateState { it.copy(maxCockroaches = safeValue) }
    }

    override fun updateBonusIntervalSeconds(value: Int) {
        val safeValue = value.coerceIn(
            GameSettingConstraints.minBonusIntervalSeconds,
            GameSettingConstraints.maxBonusIntervalSeconds
        )
        preferences.edit().putInt(KEY_BONUS_INTERVAL_SECONDS, safeValue).apply()
        updateState { it.copy(bonusIntervalSeconds = safeValue) }
    }

    override fun updateRoundDurationSeconds(value: Int) {
        val safeValue = value.coerceIn(
            GameSettingConstraints.minRoundDurationSeconds,
            GameSettingConstraints.maxRoundDurationSeconds
        )
        preferences.edit().putInt(KEY_ROUND_DURATION_SECONDS, safeValue).apply()
        updateState { it.copy(roundDurationSeconds = safeValue) }
    }

    private fun readSettings(): GameSettings {
        return GameSettings(
            speed = preferences.getInt(KEY_SPEED, GameSettingConstraints.defaultSpeed).coerceIn(
                GameSettingConstraints.minSpeed,
                GameSettingConstraints.maxSpeed
            ),
            maxCockroaches = preferences.getInt(
                KEY_MAX_COCKROACHES,
                GameSettingConstraints.defaultMaxCockroaches
            ).coerceIn(
                GameSettingConstraints.minMaxCockroaches,
                GameSettingConstraints.maxMaxCockroaches
            ),
            bonusIntervalSeconds = preferences.getInt(
                KEY_BONUS_INTERVAL_SECONDS,
                GameSettingConstraints.defaultBonusIntervalSeconds
            ).coerceIn(
                GameSettingConstraints.minBonusIntervalSeconds,
                GameSettingConstraints.maxBonusIntervalSeconds
            ),
            roundDurationSeconds = preferences.getInt(
                KEY_ROUND_DURATION_SECONDS,
                GameSettingConstraints.defaultRoundDurationSeconds
            ).coerceIn(
                GameSettingConstraints.minRoundDurationSeconds,
                GameSettingConstraints.maxRoundDurationSeconds
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
