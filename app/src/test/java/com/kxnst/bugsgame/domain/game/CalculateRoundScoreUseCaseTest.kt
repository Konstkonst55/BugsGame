package com.kxnst.bugsgame.domain.game

import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateRoundScoreUseCaseTest {
    private val useCase = CalculateRoundScoreUseCase()

    @Test
    fun scoreNeverBecomesNegative() {
        val score = useCase.execute(
            RoundScoreInput(
                points = 0,
                penalties = 100,
                difficulty = 1,
                speed = 1,
                maxCockroaches = 1,
                roundDurationSeconds = 30
            )
        )

        assertTrue(score >= 0)
    }

    @Test
    fun scoreIsBounded() {
        val score = useCase.execute(
            RoundScoreInput(
                points = 10_000,
                penalties = 0,
                difficulty = 3,
                speed = 10,
                maxCockroaches = 10,
                roundDurationSeconds = 300
            )
        )

        assertTrue(score <= 1_000)
    }
}
