package com.example.calc

import com.example.calc.domain.history.ExpressionHistory
import com.example.calc.domain.history.HistoryEntry
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Instant

class ExpressionHistoryTests {
    private val parisTimeZone = TimeZone.of("Europe/Paris")

    @Test
    fun `retainLast30Days keeps the last 30 local days inclusive`() {
        val retainedEntries = ExpressionHistory.retainLast30Days(
            entries = listOf(
                HistoryEntry("expired", Instant.parse("2026-07-11T21:59:59Z")),
                HistoryEntry("boundary", Instant.parse("2026-07-11T22:00:00Z")),
                HistoryEntry("recent", Instant.parse("2026-08-10T00:30:00Z"))
            ),
            now = Instant.parse("2026-08-10T01:00:00Z"),
            timeZone = parisTimeZone
        )

        assertEquals(
            listOf("recent", "boundary"),
            retainedEntries.map { it.expression }
        )
    }

    @Test
    fun `groupByDay groups entries by local day in reverse chronological order`() {
        val groupedEntries = ExpressionHistory.groupByDay(
            entries = listOf(
                HistoryEntry("latest", Instant.parse("2026-08-10T00:30:00Z")),
                HistoryEntry("same local day", Instant.parse("2026-08-09T23:30:00Z")),
                HistoryEntry("previous local day", Instant.parse("2026-08-09T20:00:00Z"))
            ),
            timeZone = parisTimeZone
        )

        assertEquals(
            listOf("2026-08-10", "2026-08-09"),
            groupedEntries.map { it.date.toString() }
        )
        assertEquals(
            listOf("latest", "same local day"),
            groupedEntries.first().entries.map { it.expression }
        )
        assertEquals(
            listOf("previous local day"),
            groupedEntries.last().entries.map { it.expression }
        )
    }
}
