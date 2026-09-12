package com.example.calc.domain.tracking

import com.example.calc.domain.conversion.CurrencyConverter
import com.example.calc.domain.conversion.CurrencyRates
import com.example.calc.domain.conversion.EURO_ISO
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.math.BigDecimal
import java.math.MathContext

object TrackedAmounts {
    fun groupByDay(
        amounts: List<TrackedAmount>,
        timeZone: TimeZone
    ): List<TrackedAmountDayGroup> = amounts
        .sortedByDescending { it.recordedAt }
        .groupBy { it.recordedAt.toLocalDateTime(timeZone).date }
        .entries
        .sortedByDescending { it.key }
        .map { (date, dayAmounts) -> TrackedAmountDayGroup(date, dayAmounts) }

    fun pruneOrphanSnapshots(
        amounts: List<TrackedAmount>,
        snapshots: Map<LocalDate, DayRatesSnapshot>,
        timeZone: TimeZone
    ): Map<LocalDate, DayRatesSnapshot> {
        val liveDates = amounts.mapTo(mutableSetOf()) { it.recordedAt.toLocalDateTime(timeZone).date }
        return snapshots.filterKeys { it in liveDates }
    }

    fun convert(
        groups: List<TrackedAmountDayGroup>,
        snapshots: Map<LocalDate, DayRatesSnapshot>,
        displayCurrency: String
    ): List<ConvertedDayGroup> = groups.map { group ->
        val snapshot = snapshots[group.date]
        val rates = snapshot?.let { CurrencyRates.fromMap(it.ratesMap, it.capturedAt) }

        if (rates == null || !rates.rateExists(displayCurrency)) {
            return@map ConvertedDayGroup(
                date = group.date,
                total = null,
                amounts = group.amounts.map { ConvertedAmount(it.id, amount = null) }
            )
        }

        val converter = CurrencyConverter(rates, EURO_ISO, displayCurrency)
        val totalEur = group.amounts.fold(BigDecimal.ZERO) { acc, amount ->
            acc.add(amount.amountEur, MathContext.DECIMAL64)
        }

        ConvertedDayGroup(
            date = group.date,
            total = converter.convert(totalEur),
            amounts = group.amounts.map { ConvertedAmount(it.id, converter.convert(it.amountEur)) }
        )
    }
}
