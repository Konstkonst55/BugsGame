package com.kxnst.bugsgame.presentation.game

enum class GamePhase {
    READY,
    RUNNING,
    FINISHED
}

sealed interface GameSoundEvent {
    data object BugHit : GameSoundEvent
    data object Penalty : GameSoundEvent
    data object BonusCollected : GameSoundEvent
    data class BonusActivated(val bugCount: Int) : GameSoundEvent
}

data class GameResult(
    val roundId: Long,
    val userName: String,
    val rawScore: Int,
    val penalties: Int,
    val finalScore: Int,
    val difficulty: Int,
    val roundDurationSeconds: Int,
    val speed: Int,
    val maxCockroaches: Int
)

data class GameBonus(
    val x: Float,
    val y: Float
)

data class GameState(
    val phase: GamePhase = GamePhase.READY,
    val score: Int = 0,
    val penalties: Int = 0,
    val remainingSeconds: Int = 0,
    val bugs: List<GameBug> = emptyList(),
    val bonus: GameBonus? = null,
    val result: GameResult? = null
)
