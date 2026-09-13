package com.kxnst.bugsgame.domain.zodiac

import com.kxnst.bugsgame.data.zodiac.ZodiacSign
import java.time.LocalDate
import java.time.MonthDay

class ZodiacCalculator {
    fun calculate(date: LocalDate, signs: List<ZodiacSign>): ZodiacSign? {
        val monthDay = MonthDay.from(date)
        return signs.firstOrNull { it.contains(monthDay) }
    }
}