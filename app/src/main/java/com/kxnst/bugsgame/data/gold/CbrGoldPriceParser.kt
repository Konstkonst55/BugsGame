package com.kxnst.bugsgame.data.gold

import android.util.Xml

import com.kxnst.bugsgame.domain.gold.GoldPrice

import org.xmlpull.v1.XmlPullParser

import java.io.StringReader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class CbrGoldPriceParser {
    fun parse(xml: String): GoldPrice {
        val parser = Xml.newPullParser()
        parser.setInput(StringReader(xml))

        val records = mutableListOf<MetalRecord>()
        var recordCode: String? = null
        var recordDate: LocalDate? = null
        var buy: Double? = null
        var sell: Double? = null

        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            when (parser.eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "Record" -> {
                            recordCode = parser.getAttributeValue(null, "Code")
                            recordDate = parser.getAttributeValue(null, "Date")
                                ?.let { LocalDate.parse(it, RECORD_DATE_FORMATTER) }
                            buy = null
                            sell = null
                        }

                        "Buy" -> buy = parser.nextText().toRussianDecimal()
                        "Sell" -> sell = parser.nextText().toRussianDecimal()
                    }
                }

                XmlPullParser.END_TAG -> {
                    if (parser.name == "Record" && recordCode == GOLD_METAL_CODE) {
                        val date = recordDate
                        val buyPrice = buy
                        val sellPrice = sell

                        if (date != null && buyPrice != null && sellPrice != null) {
                            records += MetalRecord(
                                date = date,
                                buy = buyPrice,
                                sell = sellPrice
                            )
                        }

                        recordCode = null
                        recordDate = null
                        buy = null
                        sell = null
                    }
                }
            }

            parser.next()
        }

        val sortedRecords = records.distinctBy(MetalRecord::date).sortedBy(MetalRecord::date)
        val latest = sortedRecords.lastOrNull()
            ?: error("The Central Bank response did not contain gold quotations")
        val previous = sortedRecords.getOrNull(sortedRecords.lastIndex - 1)

        return GoldPrice(
            date = latest.date,
            buyRublesPerGram = latest.buy,
            sellRublesPerGram = latest.sell,
            previousBuyRublesPerGram = previous?.buy
        )
    }

    private fun String.toRussianDecimal(): Double {
        return trim()
            .replace(" ", "")
            .replace('\u00A0'.toString(), "")
            .replace(',', '.')
            .toDouble()
    }

    private data class MetalRecord(
        val date: LocalDate,
        val buy: Double,
        val sell: Double
    )

    private companion object {
        const val GOLD_METAL_CODE = "1"
        val RECORD_DATE_FORMATTER: DateTimeFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.ROOT)
    }
}
