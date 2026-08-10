package com.example.calc.domain.history

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

object ExpressionHistory {
    fun retainLast30Days(
        entries: List<HistoryEntry>,
        now: Instant,
        timeZone: TimeZone
    ): List<HistoryEntry> {
        val cutoffDate = now.toLocalDateTime(timeZone).date.minus(29, DateTimeUnit.DAY)

        return entries
            .filter { it.recordedAt.toLocalDateTime(timeZone).date >= cutoffDate }
            .sortedByDescending { it.recordedAt }
    }

    fun groupByDay(
        entries: List<HistoryEntry>,
        timeZone: TimeZone
    ): List<HistoryDayGroup> = entries
        .sortedByDescending { it.recordedAt }
        .groupBy { it.recordedAt.toLocalDateTime(timeZone).date }
        .entries
        .sortedByDescending { it.key }
        .map { (date, dayEntries) -> HistoryDayGroup(date, dayEntries) }
}
