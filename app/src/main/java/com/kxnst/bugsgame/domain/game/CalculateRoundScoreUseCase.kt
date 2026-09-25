package com.kxnst.bugsgame.domain.game

import kotlin.math.roundToInt
import kotlin.math.sqrt

class CalculateRoundScoreUseCase {
    fun execute(input: RoundScoreInput): Int {
        val adjustedPoints = (
            input.points - input.penalties * PENALTY_COST
        ).coerceAtLeast(0f)

        val baselineSpawnCount = (
            input.roundDurationSeconds * MILLIS_PER_SECOND / BASELINE_SPAWN_INTERVAL_MS
        ).coerceAtLeast(1L)

        val baselineExpectedPoints = baselineSpawnCount * AVERAGE_BUG_POINTS
        val performance = (
            adjustedPoints / baselineExpectedPoints
        ).coerceIn(0f, MAX_PERFORMANCE)

        val difficultyFactor = when (input.difficulty) {
            MIN_DIFFICULTY -> DIFFICULTY_EASY_FACTOR
            MAX_DIFFICULTY -> DIFFICULTY_HARD_FACTOR
            else -> DIFFICULTY_NORMAL_FACTOR
        }

        val speedFactor = interpolateFactor(
            input.speed,
            MIN_SPEED,
            MAX_SPEED,
            MIN_SPEED_FACTOR,
            MAX_SPEED_FACTOR
        )

        val maxBugsFactor = interpolateFactor(
            input.maxCockroaches,
            MIN_MAX_BUGS,
            MAX_MAX_BUGS,
            MIN_MAX_BUGS_FACTOR,
            MAX_MAX_BUGS_FACTOR
        )

        val durationFactor = sqrt(
            (input.roundDurationSeconds.toFloat() / BASELINE_ROUND_DURATION_SECONDS)
                .coerceIn(MIN_DURATION_RATIO, MAX_DURATION_RATIO)
        ).coerceIn(MIN_DURATION_FACTOR, MAX_DURATION_FACTOR)

        return (
            performance * BASE_SCORE * difficultyFactor * speedFactor *
                maxBugsFactor * durationFactor
        ).roundToInt().coerceIn(MIN_SCORE, MAX_SCORE)
    }

    private fun interpolateFactor(
        value: Int,
        minValue: Int,
        maxValue: Int,
        minFactor: Float,
        maxFactor: Float
    ): Float {
        if (minValue == maxValue) {
            return minFactor
        }

        val normalized = (
            (value - minValue).toFloat() / (maxValue - minValue)
        ).coerceIn(0f, 1f)

        return minFactor + normalized * (maxFactor - minFactor)
    }

    private companion object {
        const val PENALTY_COST = 1.5f
        const val BASELINE_SPAWN_INTERVAL_MS = 900L
        const val MILLIS_PER_SECOND = 1000L
        const val AVERAGE_BUG_POINTS = 2f
        const val MAX_PERFORMANCE = 1.4f
        const val BASE_SCORE = 500f
        const val MIN_SCORE = 0
        const val MAX_SCORE = 1000

        const val MIN_DIFFICULTY = 1
        const val MAX_DIFFICULTY = 3
        const val DIFFICULTY_EASY_FACTOR = 0.9f
        const val DIFFICULTY_NORMAL_FACTOR = 1f
        const val DIFFICULTY_HARD_FACTOR = 1.1f

        const val MIN_SPEED = 1
        const val MAX_SPEED = 10
        const val MIN_SPEED_FACTOR = 0.85f
        const val MAX_SPEED_FACTOR = 1.15f

        const val MIN_MAX_BUGS = 1
        const val MAX_MAX_BUGS = 10
        const val MIN_MAX_BUGS_FACTOR = 0.9f
        const val MAX_MAX_BUGS_FACTOR = 1.1f

        const val BASELINE_ROUND_DURATION_SECONDS = 60f
        const val MIN_DURATION_RATIO = 0.5f
        const val MAX_DURATION_RATIO = 2f
        const val MIN_DURATION_FACTOR = 0.75f
        const val MAX_DURATION_FACTOR = 1.25f
    }
}

data class RoundScoreInput(
    val points: Int,
    val penalties: Int,
    val difficulty: Int,
    val speed: Int,
    val maxCockroaches: Int,
    val roundDurationSeconds: Int
)
