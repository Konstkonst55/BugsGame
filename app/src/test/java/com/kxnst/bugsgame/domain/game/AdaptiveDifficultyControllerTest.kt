package com.kxnst.bugsgame.domain.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveDifficultyControllerTest {
    private val controller = AdaptiveDifficultyController()

    @Test
    fun initialDifficultyDefinesBaseParameters() {
        val easy = controller.createBaseParameters(GameRules.MIN_DIFFICULTY)
        val normal = controller.createBaseParameters(GameRules.DEFAULT_DIFFICULTY)
        val hard = controller.createBaseParameters(GameRules.MAX_DIFFICULTY)

        assertTrue(easy.speed < normal.speed)
        assertTrue(normal.speed < hard.speed)
        assertTrue(easy.maxCockroaches < normal.maxCockroaches)
        assertTrue(normal.maxCockroaches < hard.maxCockroaches)
        assertTrue(easy.spawnIntervalMs > normal.spawnIntervalMs)
        assertTrue(normal.spawnIntervalMs > hard.spawnIntervalMs)
        assertTrue(easy.fastBugShare < normal.fastBugShare)
        assertTrue(normal.fastBugShare < hard.fastBugShare)
    }

    @Test
    fun highEfficiencyRaisesDifficultyByOneStep() {
        val adjustment = controller.evaluateAdjustment(
            currentAdjustment = 0,
            hits = 10,
            misses = 0,
            averageReactionTimeMs = 350L,
            points = 30
        )

        assertEquals(1, adjustment)
    }

    @Test
    fun lowEfficiencyLowersDifficultyByOneStep() {
        val adjustment = controller.evaluateAdjustment(
            currentAdjustment = 0,
            hits = 0,
            misses = 5,
            averageReactionTimeMs = 0L,
            points = 0
        )

        assertEquals(-1, adjustment)
    }

    @Test
    fun insufficientSamplesDoNotChangeDifficulty() {
        val adjustment = controller.evaluateAdjustment(
            currentAdjustment = 1,
            hits = 1,
            misses = 1,
            averageReactionTimeMs = 400L,
            points = 2
        )

        assertEquals(1, adjustment)
    }


    @Test
    fun eachAdjustmentStepChangesAllFourGameParameters() {
        val base = controller.createBaseParameters(GameRules.DEFAULT_DIFFICULTY)
        val easier = controller.createParameters(base, -1)
        val harder = controller.createParameters(base, 1)

        assertTrue(easier.speed < base.speed)
        assertTrue(easier.maxCockroaches < base.maxCockroaches)
        assertTrue(easier.spawnIntervalMs > base.spawnIntervalMs)
        assertTrue(easier.fastBugShare < base.fastBugShare)
        assertTrue(harder.speed > base.speed)
        assertTrue(harder.maxCockroaches > base.maxCockroaches)
        assertTrue(harder.spawnIntervalMs < base.spawnIntervalMs)
        assertTrue(harder.fastBugShare > base.fastBugShare)
    }

    @Test
    fun adjustmentAndParametersStayWithinLimits() {
        val base = controller.createBaseParameters(GameRules.MAX_DIFFICULTY)
        val raisedAdjustment = controller.evaluateAdjustment(
            currentAdjustment = 2,
            hits = 10,
            misses = 0,
            averageReactionTimeMs = 250L,
            points = 100
        )
        val loweredAdjustment = controller.evaluateAdjustment(
            currentAdjustment = -2,
            hits = 0,
            misses = 10,
            averageReactionTimeMs = 0L,
            points = 0
        )
        val hardest = controller.createParameters(base, raisedAdjustment)
        val easiest = controller.createParameters(base, loweredAdjustment)

        assertEquals(2, raisedAdjustment)
        assertEquals(-2, loweredAdjustment)
        assertTrue(hardest.speed in GameRules.MIN_SPEED..GameRules.MAX_SPEED)
        assertTrue(hardest.maxCockroaches in GameRules.MIN_MAX_COCKROACHES..GameRules.MAX_MAX_COCKROACHES)
        assertTrue(easiest.speed in GameRules.MIN_SPEED..GameRules.MAX_SPEED)
        assertTrue(easiest.maxCockroaches in GameRules.MIN_MAX_COCKROACHES..GameRules.MAX_MAX_COCKROACHES)
        assertTrue(easiest.spawnIntervalMs >= 350L)
        assertTrue(hardest.spawnIntervalMs <= 2_000L)
        assertTrue(easiest.fastBugShare in 0.10f..0.70f)
        assertTrue(hardest.fastBugShare in 0.10f..0.70f)
    }
}
