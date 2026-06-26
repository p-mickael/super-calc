package com.example.calc.domain.conversion

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import kotlin.time.Clock
import kotlin.time.Instant

@ConsistentCopyVisibility
data class CurrencyRates private constructor(
    val ratesMap: Map<String, BigDecimal>,
    val updatedAt: Instant
) {
    companion object {
        fun fromMap(rates: Map<String, BigDecimal>, updatedAt: Instant): CurrencyRates {
            val ratesIncludedEur =
                rates.takeIf { EURO_ISO in rates } ?: (rates + (EURO_ISO to BigDecimal.ONE))
            return CurrencyRates(ratesIncludedEur, updatedAt)
        }
    }

    val isFresh: Boolean
        get() {
            val timeZone = TimeZone.of("Europe/Paris")
            val now = Clock.System.now().toLocalDateTime(timeZone)
            val referenceDay = if (now.hour >= 16) now.date
            else now.date.minus(1, DateTimeUnit.DAY)
            val lastUpdate = updatedAt.toLocalDateTime(timeZone)

            return lastUpdate >= referenceDay.atTime(16, 0)
        }

    fun getRate(isoName: String): BigDecimal = ratesMap[isoName]
        ?: throw UnknownCurrencyException(isoName)

    fun rateExists(isoName: String): Boolean = ratesMap.containsKey(isoName)
}