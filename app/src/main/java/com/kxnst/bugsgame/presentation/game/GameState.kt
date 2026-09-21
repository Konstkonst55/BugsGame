package com.kxnst.bugsgame.presentation.game

enum class GamePhase {
    READY,
    RUNNING,
    FINISHED
}

data class GameResult(
    val roundId: Long,
    val score: Int,
    val penalties: Int,
    val roundDurationSeconds: Int,
    val speed: Int,
    val maxCockroaches: Int
)

data class GameState(
    val phase: GamePhase = GamePhase.READY,
    val score: Int = 0,
    val penalties: Int = 0,
    val remainingSeconds: Int = 0,
    val bugs: List<GameBug> = emptyList(),
    val result: GameResult? = null
)
