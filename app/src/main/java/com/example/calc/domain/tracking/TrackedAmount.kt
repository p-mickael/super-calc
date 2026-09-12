package com.example.calc.domain.tracking

import kotlinx.datetime.LocalDate
import java.math.BigDecimal
import kotlin.time.Instant

data class TrackedAmount(
    val id: String,
    val amountEur: BigDecimal,
    val recordedAt: Instant
)

data class TrackedAmountDayGroup(
    val date: LocalDate,
    val amounts: List<TrackedAmount>
)

data class DayRatesSnapshot(
    val date: LocalDate,
    val ratesMap: Map<String, BigDecimal>,
    val capturedAt: Instant
)

data class ConvertedAmount(
    val trackedAmountId: String,
    val amount: BigDecimal?
)

data class ConvertedDayGroup(
    val date: LocalDate,
    val total: BigDecimal?,
    val amounts: List<ConvertedAmount>
)
