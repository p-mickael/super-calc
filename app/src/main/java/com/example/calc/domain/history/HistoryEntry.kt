package com.example.calc.domain.history

import com.example.calc.domain.calculator.model.Token
import kotlinx.datetime.LocalDate
import kotlin.time.Instant

data class HistoryEntry(
    val tokens: List<Token>,
    val recordedAt: Instant
)

data class HistoryDayGroup(
    val date: LocalDate,
    val entries: List<HistoryEntry>
)
