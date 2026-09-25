package com.kxnst.bugsgame.data.settings

import android.content.Context

import com.kxnst.bugsgame.domain.game.GameRules

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
            GameRules.MIN_SPEED,
            GameRules.MAX_SPEED
        )

        preferences.edit().putInt(KEY_SPEED, safeValue).apply()
        updateState { it.copy(speed = safeValue) }
    }

    override fun updateMaxCockroaches(value: Int) {
        val safeValue = value.coerceIn(
            GameRules.MIN_MAX_COCKROACHES,
            GameRules.MAX_MAX_COCKROACHES
        )

        preferences.edit().putInt(KEY_MAX_COCKROACHES, safeValue).apply()
        updateState { it.copy(maxCockroaches = safeValue) }
    }

    override fun updateBonusIntervalSeconds(value: Int) {
        val safeValue = value.coerceIn(
            GameRules.MIN_BONUS_INTERVAL_SECONDS,
            GameRules.MAX_BONUS_INTERVAL_SECONDS
        )

        preferences.edit().putInt(KEY_BONUS_INTERVAL_SECONDS, safeValue).apply()
        updateState { it.copy(bonusIntervalSeconds = safeValue) }
    }

    override fun updateRoundDurationSeconds(value: Int) {
        val safeValue = value.coerceIn(
            GameRules.MIN_ROUND_DURATION_SECONDS,
            GameRules.MAX_ROUND_DURATION_SECONDS
        )

        preferences.edit().putInt(KEY_ROUND_DURATION_SECONDS, safeValue).apply()
        updateState { it.copy(roundDurationSeconds = safeValue) }
    }

    private fun readSettings(): GameSettings {
        return GameSettings(
            speed = preferences.getInt(
                KEY_SPEED,
                GameRules.DEFAULT_SPEED
            ).coerceIn(
                GameRules.MIN_SPEED,
                GameRules.MAX_SPEED
            ),
            maxCockroaches = preferences.getInt(
                KEY_MAX_COCKROACHES,
                GameRules.DEFAULT_MAX_COCKROACHES
            ).coerceIn(
                GameRules.MIN_MAX_COCKROACHES,
                GameRules.MAX_MAX_COCKROACHES
            ),
            bonusIntervalSeconds = preferences.getInt(
                KEY_BONUS_INTERVAL_SECONDS,
                GameRules.DEFAULT_BONUS_INTERVAL_SECONDS
            ).coerceIn(
                GameRules.MIN_BONUS_INTERVAL_SECONDS,
                GameRules.MAX_BONUS_INTERVAL_SECONDS
            ),
            roundDurationSeconds = preferences.getInt(
                KEY_ROUND_DURATION_SECONDS,
                GameRules.DEFAULT_ROUND_DURATION_SECONDS
            ).coerceIn(
                GameRules.MIN_ROUND_DURATION_SECONDS,
                GameRules.MAX_ROUND_DURATION_SECONDS
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
