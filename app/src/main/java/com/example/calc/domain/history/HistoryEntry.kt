package com.example.calc.domain.history

import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class HistoryEntry(
    val expression: String,
    val recordedAt: Instant
)

data class HistoryDayGroup(
    val date: LocalDate,
    val entries: List<HistoryEntry>
)
