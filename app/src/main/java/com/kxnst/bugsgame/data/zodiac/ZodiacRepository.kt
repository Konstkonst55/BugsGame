package com.kxnst.bugsgame.data.zodiac

interface ZodiacRepository {
    suspend fun getSigns(): List<ZodiacSign>
}
