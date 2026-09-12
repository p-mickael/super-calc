package com.example.calc

import com.example.calc.domain.tracking.DayRatesSnapshot
import com.example.calc.domain.tracking.TrackedAmount
import com.example.calc.domain.tracking.TrackedAmounts
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import kotlin.time.Instant

class TrackedAmountsTests {
    private val parisTimeZone = TimeZone.of("Europe/Paris")

    private fun taggedAmount(id: String, amountEur: String, recordedAt: Instant) =
        TrackedAmount(id, BigDecimal(amountEur), recordedAt)

    @Test
    fun `groupByDay groups amounts by local day in reverse chronological order, duplicates kept`() {
        val groupedAmounts = TrackedAmounts.groupByDay(
            amounts = listOf(
                taggedAmount("latest", "10", Instant.parse("2026-08-10T00:30:00Z")),
                taggedAmount("same local day a", "5", Instant.parse("2026-08-09T23:30:00Z")),
                taggedAmount("same local day b", "5", Instant.parse("2026-08-09T23:30:00Z")),
                taggedAmount("previous local day", "1", Instant.parse("2026-08-09T20:00:00Z"))
            ),
            timeZone = parisTimeZone
        )

        assertEquals(
            listOf("2026-08-10", "2026-08-09"),
            groupedAmounts.map { it.date.toString() }
        )
        assertEquals(
            listOf("latest", "same local day a", "same local day b"),
            groupedAmounts.first().amounts.map { it.id }
        )
        assertEquals(
            listOf("previous local day"),
            groupedAmounts.last().amounts.map { it.id }
        )
    }

    @Test
    fun `pruneOrphanSnapshots drops snapshots whose day has no remaining amount`() {
        val dayWithAmount = LocalDate.parse("2026-08-10")
        val orphanDay = LocalDate.parse("2026-08-09")

        val pruned = TrackedAmounts.pruneOrphanSnapshots(
            amounts = listOf(taggedAmount("only", "10", Instant.parse("2026-08-10T10:00:00Z"))),
            snapshots = mapOf(
                dayWithAmount to DayRatesSnapshot(dayWithAmount, mapOf("EUR" to BigDecimal.ONE), Instant.parse("2026-08-10T10:00:00Z")),
                orphanDay to DayRatesSnapshot(orphanDay, mapOf("EUR" to BigDecimal.ONE), Instant.parse("2026-08-09T10:00:00Z"))
            ),
            timeZone = parisTimeZone
        )

        assertEquals(setOf(dayWithAmount), pruned.keys)
    }

    @Test
    fun `convert uses each day's own locked rates, not the other day's`() {
        val dayA = LocalDate.parse("2026-08-09")
        val dayB = LocalDate.parse("2026-08-10")

        val groups = TrackedAmounts.groupByDay(
            amounts = listOf(
                taggedAmount("a", "10", Instant.parse("2026-08-09T10:00:00Z")),
                taggedAmount("b", "10", Instant.parse("2026-08-10T10:00:00Z"))
            ),
            timeZone = parisTimeZone
        )
        val snapshots = mapOf(
            dayA to DayRatesSnapshot(dayA, mapOf("EUR" to BigDecimal.ONE, "USD" to BigDecimal("2")), Instant.parse("2026-08-09T10:00:00Z")),
            dayB to DayRatesSnapshot(dayB, mapOf("EUR" to BigDecimal.ONE, "USD" to BigDecimal("3")), Instant.parse("2026-08-10T10:00:00Z"))
        )

        val converted = TrackedAmounts.convert(groups, snapshots, "USD")

        val convertedDayA = converted.single { it.date == dayA }
        val convertedDayB = converted.single { it.date == dayB }
        assertEquals(0, BigDecimal("20").compareTo(convertedDayA.total!!))
        assertEquals(0, BigDecimal("30").compareTo(convertedDayB.total!!))
    }

    @Test
    fun `convert returns null total and null amounts when the display currency is absent from the day's snapshot`() {
        val day = LocalDate.parse("2026-08-10")
        val groups = TrackedAmounts.groupByDay(
            amounts = listOf(taggedAmount("a", "10", Instant.parse("2026-08-10T10:00:00Z"))),
            timeZone = parisTimeZone
        )
        val snapshots = mapOf(
            day to DayRatesSnapshot(day, mapOf("EUR" to BigDecimal.ONE), Instant.parse("2026-08-10T10:00:00Z"))
        )

        val converted = TrackedAmounts.convert(groups, snapshots, "USD").single()

        assertNull(converted.total)
        assertEquals(listOf<BigDecimal?>(null), converted.amounts.map { it.amount })
    }

    @Test
    fun `convert returns null when a day has no snapshot at all`() {
        val day = LocalDate.parse("2026-08-10")
        val groups = TrackedAmounts.groupByDay(
            amounts = listOf(taggedAmount("a", "10", Instant.parse("2026-08-10T10:00:00Z"))),
            timeZone = parisTimeZone
        )

        val converted = TrackedAmounts.convert(groups, emptyMap(), "EUR").single()

        assertNull(converted.total)
    }
}
