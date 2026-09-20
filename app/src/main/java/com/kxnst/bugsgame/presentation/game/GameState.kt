package com.kxnst.bugsgame.presentation.game

data class GameState(
    val score: Int = 0,
    val penalties: Int = 0,
    val bugs: List<GameBug> = emptyList()
)
