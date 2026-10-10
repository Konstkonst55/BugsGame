package com.kxnst.bugsgame.data.gold

import android.content.Context

import com.kxnst.bugsgame.domain.gold.GoldPrice
import com.kxnst.bugsgame.domain.gold.GoldPriceRepository

import kotlinx.coroutines.CancellationException

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.core.content.edit

class CbrGoldPriceRepository(
    private val api: CbrMetalsApi,
    context: Context,
    private val parser: CbrGoldPriceParser = CbrGoldPriceParser()
) : GoldPriceRepository {
    private val preferences = context.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    override suspend fun getLatestGoldPrice(forceRefresh: Boolean): GoldPrice? {
        val cached = readCachedPrice()
        val today = LocalDate.now()

        if (!forceRefresh && cached != null && !cached.date.isBefore(today)) {
            return cached
        }

        return try {
            val startDate = today.minusDays(LOOKBACK_DAYS)
            val response = api.getMetalsQuotes(
                startDate.format(REQUEST_DATE_FORMATTER),
                today.format(REQUEST_DATE_FORMATTER)
            )
            parser.parse(response).also(::saveCachedPrice)
        } catch (exception: CancellationException) {
            throw exception
        } catch (_: Exception) {
            cached
        }
    }

    private fun readCachedPrice(): GoldPrice? {
        val date = preferences.getString(KEY_DATE, null)
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: return null
        val buy = preferences.getString(KEY_BUY, null)?.toDoubleOrNull() ?: return null
        val sell = preferences.getString(KEY_SELL, null)?.toDoubleOrNull() ?: return null
        val previousBuy = preferences.getString(KEY_PREVIOUS_BUY, null)?.toDoubleOrNull()

        return GoldPrice(
            date = date,
            buyRublesPerGram = buy,
            sellRublesPerGram = sell,
            previousBuyRublesPerGram = previousBuy
        )
    }

    private fun saveCachedPrice(price: GoldPrice) {
        preferences.edit {
            putString(KEY_DATE, price.date.toString())
                .putString(KEY_BUY, price.buyRublesPerGram.toString())
                .putString(KEY_SELL, price.sellRublesPerGram.toString())
                .putString(KEY_PREVIOUS_BUY, price.previousBuyRublesPerGram?.toString())
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "cbr_gold_price_cache"
        const val LOOKBACK_DAYS = 10L
        const val KEY_DATE = "date"
        const val KEY_BUY = "buy"
        const val KEY_SELL = "sell"
        const val KEY_PREVIOUS_BUY = "previous_buy"
        val REQUEST_DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ROOT)
    }
}
