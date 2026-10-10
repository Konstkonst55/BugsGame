package com.kxnst.bugsgame.domain.gold

interface GoldPriceRepository {
    suspend fun getLatestGoldPrice(forceRefresh: Boolean = false): GoldPrice?
}
