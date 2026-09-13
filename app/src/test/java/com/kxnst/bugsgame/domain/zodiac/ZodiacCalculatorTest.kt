package com.kxnst.bugsgame.domain.zodiac

import com.kxnst.bugsgame.data.zodiac.ZodiacSign
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ZodiacCalculatorTest {
    private val signs = listOf(
        ZodiacSign("Овен", "", "", 3, 21, 4, 19),
        ZodiacSign("Телец", "", "", 4, 20, 5, 20),
        ZodiacSign("Козерог", "", "", 12, 22, 1, 19)
    )
    private val calculator = ZodiacCalculator()

    @Test
    fun returnsAriesAtStartBoundary() {
        assertEquals("Овен", calculator.calculate(LocalDate.of(2026, 3, 21), signs)?.name)
    }

    @Test
    fun returnsTaurusAtEndBoundary() {
        assertEquals("Телец", calculator.calculate(LocalDate.of(2026, 5, 20), signs)?.name)
    }

    @Test
    fun returnsCapricornAcrossYearBoundary() {
        assertEquals("Козерог", calculator.calculate(LocalDate.of(2026, 1, 19), signs)?.name)
        assertEquals("Козерог", calculator.calculate(LocalDate.of(2026, 12, 22), signs)?.name)
    }
}
