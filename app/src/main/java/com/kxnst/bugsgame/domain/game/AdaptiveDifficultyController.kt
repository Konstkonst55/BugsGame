package com.kxnst.bugsgame.domain.game

import kotlin.math.roundToInt

class AdaptiveDifficultyController {
    fun createBaseParameters(difficulty: Int): AdaptiveDifficultyParameters {
        return when (difficulty.coerceIn(GameRules.MIN_DIFFICULTY, GameRules.MAX_DIFFICULTY)) {
            GameRules.MIN_DIFFICULTY -> AdaptiveDifficultyParameters(
                speed = EASY_BASE_SPEED,
                maxCockroaches = EASY_BASE_MAX_COCKROACHES,
                spawnIntervalMs = GameRules.SPAWN_INTERVAL_EASY_MS,
                fastBugShare = EASY_BASE_FAST_BUG_SHARE
            )

            GameRules.MAX_DIFFICULTY -> AdaptiveDifficultyParameters(
                speed = HARD_BASE_SPEED,
                maxCockroaches = HARD_BASE_MAX_COCKROACHES,
                spawnIntervalMs = GameRules.SPAWN_INTERVAL_HARD_MS,
                fastBugShare = HARD_BASE_FAST_BUG_SHARE
            )

            else -> AdaptiveDifficultyParameters(
                speed = NORMAL_BASE_SPEED,
                maxCockroaches = NORMAL_BASE_MAX_COCKROACHES,
                spawnIntervalMs = GameRules.SPAWN_INTERVAL_NORMAL_MS,
                fastBugShare = NORMAL_BASE_FAST_BUG_SHARE
            )
        }
    }

    fun evaluateAdjustment(
        currentAdjustment: Int,
        hits: Int,
        misses: Int,
        averageReactionTimeMs: Long,
        points: Int
    ): Int {
        val attempts = hits + misses
        if (attempts < MIN_ATTEMPTS_FOR_ADJUSTMENT) {
            return currentAdjustment.coerceIn(MIN_ADJUSTMENT, MAX_ADJUSTMENT)
        }

        val catchRate = hits.toFloat() / attempts
        val reactionQuality = if (hits == 0) {
            0f
        } else {
            (
                (MAX_REACTION_TIME_MS - averageReactionTimeMs).toFloat() /
                    (MAX_REACTION_TIME_MS - MIN_REACTION_TIME_MS).toFloat()
                ).coerceIn(0f, 1f)
        }
        val scoreRate = points.toFloat() / ANALYSIS_WINDOW_SECONDS
        val scoreRateQuality = (scoreRate / TARGET_SCORE_PER_SECOND).coerceIn(0f, 1f)

        val efficiency = catchRate * CATCH_RATE_WEIGHT +
            reactionQuality * REACTION_WEIGHT +
            scoreRateQuality * SCORE_RATE_WEIGHT

        val adjustment = when {
            efficiency < LOWER_EFFICIENCY_LIMIT -> currentAdjustment - ADJUSTMENT_STEP
            efficiency > UPPER_EFFICIENCY_LIMIT -> currentAdjustment + ADJUSTMENT_STEP
            else -> currentAdjustment
        }

        return adjustment.coerceIn(MIN_ADJUSTMENT, MAX_ADJUSTMENT)
    }

    fun createParameters(
        baseParameters: AdaptiveDifficultyParameters,
        adjustment: Int
    ): AdaptiveDifficultyParameters {
        val safeAdjustment = adjustment.coerceIn(MIN_ADJUSTMENT, MAX_ADJUSTMENT)
        val spawnMultiplier = 1f + safeAdjustment * SPAWN_STEP_MULTIPLIER

        return AdaptiveDifficultyParameters(
            speed = (baseParameters.speed + safeAdjustment).coerceIn(
                GameRules.MIN_SPEED,
                GameRules.MAX_SPEED
            ),
            maxCockroaches = (baseParameters.maxCockroaches + safeAdjustment).coerceIn(
                GameRules.MIN_MAX_COCKROACHES,
                GameRules.MAX_MAX_COCKROACHES
            ),
            spawnIntervalMs = (baseParameters.spawnIntervalMs / spawnMultiplier)
                .roundToInt()
                .toLong()
                .coerceIn(MIN_SPAWN_INTERVAL_MS, MAX_SPAWN_INTERVAL_MS),
            fastBugShare = (
                baseParameters.fastBugShare + safeAdjustment * FAST_BUG_SHARE_STEP
                ).coerceIn(MIN_FAST_BUG_SHARE, MAX_FAST_BUG_SHARE)
        )
    }

    private companion object {
        const val EASY_BASE_SPEED = 3
        const val NORMAL_BASE_SPEED = 5
        const val HARD_BASE_SPEED = 7

        const val EASY_BASE_MAX_COCKROACHES = 2
        const val NORMAL_BASE_MAX_COCKROACHES = 3
        const val HARD_BASE_MAX_COCKROACHES = 5

        const val EASY_BASE_FAST_BUG_SHARE = 0.15f
        const val NORMAL_BASE_FAST_BUG_SHARE = 0.33f
        const val HARD_BASE_FAST_BUG_SHARE = 0.50f

        const val MIN_ATTEMPTS_FOR_ADJUSTMENT = 3
        const val ANALYSIS_WINDOW_SECONDS = 15f
        const val TARGET_SCORE_PER_SECOND = 0.4f
        const val MIN_REACTION_TIME_MS = 250L
        const val MAX_REACTION_TIME_MS = 1_500L

        const val CATCH_RATE_WEIGHT = 0.5f
        const val REACTION_WEIGHT = 0.3f
        const val SCORE_RATE_WEIGHT = 0.2f
        const val LOWER_EFFICIENCY_LIMIT = 0.35f
        const val UPPER_EFFICIENCY_LIMIT = 0.70f

        const val MIN_ADJUSTMENT = -2
        const val MAX_ADJUSTMENT = 2
        const val ADJUSTMENT_STEP = 1
        const val SPAWN_STEP_MULTIPLIER = 0.10f
        const val FAST_BUG_SHARE_STEP = 0.08f
        const val MIN_FAST_BUG_SHARE = 0.10f
        const val MAX_FAST_BUG_SHARE = 0.70f
        const val MIN_SPAWN_INTERVAL_MS = 350L
        const val MAX_SPAWN_INTERVAL_MS = 2_000L
    }
}

data class AdaptiveDifficultyParameters(
    val speed: Int,
    val maxCockroaches: Int,
    val spawnIntervalMs: Long,
    val fastBugShare: Float
)
