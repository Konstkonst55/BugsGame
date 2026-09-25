package com.kxnst.bugsgame.domain.game

object GameRules {
    const val MIN_DIFFICULTY = 1
    const val DEFAULT_DIFFICULTY = 2
    const val MAX_DIFFICULTY = 3

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

    const val SPAWN_INTERVAL_EASY_MS = 1400L
    const val SPAWN_INTERVAL_NORMAL_MS = 900L
    const val SPAWN_INTERVAL_HARD_MS = 500L

    const val MILLIS_PER_SECOND = 1000L
}
