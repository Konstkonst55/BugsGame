package com.kxnst.bugsgame.data.zodiac

import java.time.MonthDay

class ZodiacSign(
    val name: String,
    val contentDescription: String,
    val iconResourceId: Int,
    monthStart: Int,
    dayStart: Int,
    monthEnd: Int,
    dayEnd: Int
) {
    private val start: MonthDay = MonthDay.of(monthStart, dayStart)
    private val end: MonthDay = MonthDay.of(monthEnd, dayEnd)

    fun contains(monthDay: MonthDay): Boolean {
        return if (start <= end) {
            monthDay in start..end
        } else {
            monthDay >= start || monthDay <= end
        }
    }
}
