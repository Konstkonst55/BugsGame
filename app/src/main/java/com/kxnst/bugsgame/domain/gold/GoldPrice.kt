package com.kxnst.bugsgame.domain.gold

import java.time.LocalDate

data class GoldPrice(
    val date: LocalDate,
    val buyRublesPerGram: Double,
    val sellRublesPerGram: Double,
    val previousBuyRublesPerGram: Double?
) {
    val trend: GoldPriceTrend
        get() = when {
            previousBuyRublesPerGram == null -> GoldPriceTrend.STABLE
            buyRublesPerGram > previousBuyRublesPerGram -> GoldPriceTrend.UP
            buyRublesPerGram < previousBuyRublesPerGram -> GoldPriceTrend.DOWN
            else -> GoldPriceTrend.STABLE
        }
}

enum class GoldPriceTrend {
    UP,
    DOWN,
    STABLE
}
