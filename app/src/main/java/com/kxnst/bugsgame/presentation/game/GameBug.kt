package com.kxnst.bugsgame.presentation.game

data class GameBug(
    val id: Long,
    val type: BugType,
    val x: Float,
    val y: Float,
    val velocityX: Float,
    val velocityY: Float
)
