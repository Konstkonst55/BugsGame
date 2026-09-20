package com.kxnst.bugsgame.presentation.game

enum class BugType(
    val points: Int,
    val speedMultiplier: Float
) {
    BEETLE(1, 0.8f),
    ANT(2, 1.0f),
    FLY(3, 1.2f)
}
