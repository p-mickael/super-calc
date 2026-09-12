package com.example.calc.domain.tracking

import kotlinx.datetime.LocalDate

interface TrackedAmountStore {
    suspend fun readAmounts(): List<TrackedAmount>
    suspend fun writeAmounts(amounts: List<TrackedAmount>)
    suspend fun readDayRates(): Map<LocalDate, DayRatesSnapshot>
    suspend fun writeDayRates(snapshots: Map<LocalDate, DayRatesSnapshot>)
    suspend fun clear()
}
