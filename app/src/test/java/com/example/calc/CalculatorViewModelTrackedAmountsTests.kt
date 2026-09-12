package com.example.calc

import com.example.calc.domain.AppPreferenceStore
import com.example.calc.domain.CalculatorMode
import com.example.calc.domain.conversion.CurrencyRates
import com.example.calc.domain.conversion.RatesRepository
import com.example.calc.domain.history.ExpressionHistoryStore
import com.example.calc.domain.history.HistoryEntry
import com.example.calc.domain.tracking.DayRatesSnapshot
import com.example.calc.domain.tracking.TrackedAmount
import com.example.calc.domain.tracking.TrackedAmountStore
import com.example.calc.ui.screen.CalculatorViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import kotlin.time.Clock
import kotlin.time.Instant

class CalculatorViewModelTrackedAmountsTests {
    @Test
    fun `saving an amount converts it back to EUR`() {
        val store = FakeTrackedAmountStore()
        val ratesRepository = FakeRatesRepository()
        val viewModel = createViewModel(trackedAmountStore = store, ratesRepository = ratesRepository)
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("1")

        viewModel.onSaveAmount()

        val saved = store.storedAmounts().single()
        // preview is 1 EUR -> 160 JPY (rate JPY=160, EUR=1); saving must convert back to 1 EUR
        assertEquals(0, BigDecimal("1").compareTo(saved.amountEur))
    }

    @Test
    fun `first save of a local day creates the day's rate snapshot`() {
        val store = FakeTrackedAmountStore()
        val viewModel = createViewModel(trackedAmountStore = store)
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("1")

        viewModel.onSaveAmount()

        val today = LocalDate.parse("2026-08-10")
        val snapshot = store.storedDayRates().getValue(today)
        assertEquals(BigDecimal("160"), snapshot.ratesMap["JPY"])
        assertEquals(BigDecimal.ONE, snapshot.ratesMap["EUR"])
    }

    @Test
    fun `a second save the same day does not overwrite the day's snapshot`() {
        val store = FakeTrackedAmountStore()
        val ratesRepository = FakeRatesRepository()
        val viewModel = createViewModel(trackedAmountStore = store, ratesRepository = ratesRepository)
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("1")
        viewModel.onSaveAmount()

        ratesRepository.jpyRate = BigDecimal("200")
        viewModel.onConversionTargetChanged("JPY")
        viewModel.onClear()
        viewModel.enter("1")
        viewModel.onSaveAmount()

        val today = LocalDate.parse("2026-08-10")
        assertEquals(1, store.storedDayRates().size)
        assertEquals(BigDecimal("160"), store.storedDayRates().getValue(today).ratesMap["JPY"])

        val amounts = store.storedAmounts()
        assertEquals(2, amounts.size)
        // the second entry's own EUR amount reflects the NEW rate (200), not the frozen snapshot
        assertEquals(0, BigDecimal("1").compareTo(amounts[0].amountEur))
    }

    @Test
    fun `a new local day creates a second snapshot`() {
        val store = FakeTrackedAmountStore()
        val viewModel = createViewModel(trackedAmountStore = store, now = Instant.parse("2026-08-10T23:50:00Z"))
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("1")
        viewModel.onSaveAmount()

        val nextDayViewModel = createViewModel(
            trackedAmountStore = store,
            now = Instant.parse("2026-08-12T00:10:00Z")
        )
        nextDayViewModel.onModeChanged(CalculatorMode.CONVERTER)
        nextDayViewModel.enter("1")
        nextDayViewModel.onSaveAmount()

        assertEquals(2, store.storedDayRates().size)
    }

    @Test
    fun `duplicates are allowed`() {
        val store = FakeTrackedAmountStore()
        val viewModel = createViewModel(trackedAmountStore = store)
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("1")

        viewModel.onSaveAmount()
        viewModel.onSaveAmount()

        val amounts = store.storedAmounts()
        assertEquals(2, amounts.size)
        assertTrue(amounts[0].id != amounts[1].id)
    }

    @Test
    fun `deleting one amount of two on the same day keeps the day's snapshot`() {
        val store = FakeTrackedAmountStore()
        val viewModel = createViewModel(trackedAmountStore = store)
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("1")
        viewModel.onSaveAmount()
        viewModel.onSaveAmount()

        val remainingId = store.storedAmounts().first().id
        val deletedId = store.storedAmounts().last().id
        viewModel.onTrackedAmountDeleted(deletedId)

        assertEquals(listOf(remainingId), store.storedAmounts().map { it.id })
        assertEquals(1, store.storedDayRates().size)
    }

    @Test
    fun `deleting the last amount of a day removes that day's snapshot`() {
        val store = FakeTrackedAmountStore()
        val viewModel = createViewModel(trackedAmountStore = store)
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("1")
        viewModel.onSaveAmount()

        val id = store.storedAmounts().single().id
        viewModel.onTrackedAmountDeleted(id)

        assertTrue(store.storedAmounts().isEmpty())
        assertTrue(store.storedDayRates().isEmpty())
    }

    @Test
    fun `clear all empties amounts, snapshots and the visible groups`() {
        val store = FakeTrackedAmountStore()
        val viewModel = createViewModel(trackedAmountStore = store)
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("1")
        viewModel.onSaveAmount()
        viewModel.onTrackedAmountsRequested()

        viewModel.onClearTrackedAmounts()

        assertTrue(store.storedAmounts().isEmpty())
        assertTrue(store.storedDayRates().isEmpty())
        assertTrue(viewModel.state.value.trackedAmounts.groups.isEmpty())
    }

    @Test
    fun `changing the display currency persists it and re-converts the visible totals`() {
        val preferenceStore = FakeAppPreferenceStore()
        val store = FakeTrackedAmountStore()
        val viewModel = createViewModel(preferenceStore = preferenceStore, trackedAmountStore = store)
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("1")
        viewModel.onSaveAmount()

        viewModel.onTrackedAmountCurrencyChanged("JPY")

        assertEquals("JPY", viewModel.state.value.trackedAmounts.displayCurrency)
        assertEquals("JPY", preferenceStore.trackedAmountCurrency)
        val total = viewModel.state.value.trackedAmounts.groups.single().total
        assertEquals(0, BigDecimal("160").compareTo(total!!))
    }

    @Test
    fun `the persisted display currency is restored on restart`() {
        val preferenceStore = FakeAppPreferenceStore()
        preferenceStore.trackedAmountCurrency = "JPY"
        val viewModel = createViewModel(preferenceStore = preferenceStore)

        viewModel.onForeground()

        assertEquals("JPY", viewModel.state.value.trackedAmounts.displayCurrency)
    }

    @Test
    fun `saving is a no-op in calculator mode`() {
        val store = FakeTrackedAmountStore()
        val viewModel = createViewModel(trackedAmountStore = store)
        viewModel.onModeChanged(CalculatorMode.CALCULATOR)
        viewModel.enter("1")

        viewModel.onSaveAmount()

        assertTrue(store.storedAmounts().isEmpty())
    }

    @Test
    fun `saving is a no-op when there is no preview`() {
        val store = FakeTrackedAmountStore()
        val viewModel = createViewModel(trackedAmountStore = store)
        viewModel.onModeChanged(CalculatorMode.CONVERTER)

        viewModel.onSaveAmount()

        assertTrue(store.storedAmounts().isEmpty())
    }

    @Test
    fun `canSaveAmount is true only in converter mode with a preview value`() {
        val viewModel = createViewModel()

        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        assertEquals(false, viewModel.state.value.canSaveAmount)

        viewModel.enter("1")
        assertEquals(true, viewModel.state.value.canSaveAmount)

        viewModel.onModeChanged(CalculatorMode.CALCULATOR)
        assertEquals(false, viewModel.state.value.canSaveAmount)
    }

    private fun createViewModel(
        preferenceStore: FakeAppPreferenceStore = FakeAppPreferenceStore(),
        trackedAmountStore: FakeTrackedAmountStore = FakeTrackedAmountStore(),
        ratesRepository: FakeRatesRepository = FakeRatesRepository(),
        now: Instant = Instant.parse("2026-08-10T10:00:00Z")
    ): CalculatorViewModel {
        var counter = 0
        return CalculatorViewModel(
            repository = ratesRepository,
            appPreferenceStore = preferenceStore,
            expressionHistoryStore = FakeExpressionHistoryStore(),
            trackedAmountStore = trackedAmountStore,
            clock = FixedClock(now),
            timeZoneProvider = { TimeZone.of("Europe/Paris") },
            externalScope = CoroutineScope(Dispatchers.Unconfined),
            idGenerator = { "id-${counter++}" }
        )
    }

    private fun CalculatorViewModel.enter(keys: String) {
        keys.forEach { key ->
            when (key) {
                in '0'..'9' -> onDigit(key)
                '.', ',' -> onDot()
                else -> error("Unknown key: $key")
            }
        }
    }

    private class FakeAppPreferenceStore : AppPreferenceStore {
        private var lastCalculatorMode: CalculatorMode? = null
        private var lastCurrencyPair: Pair<String, String>? = null
        var trackedAmountCurrency: String? = null

        override suspend fun getLastCalculatorMode(): CalculatorMode? = lastCalculatorMode

        override suspend fun saveLastCalculatorMode(calculatorMode: CalculatorMode) {
            lastCalculatorMode = calculatorMode
        }

        override suspend fun getLastCurrencyPair(): Pair<String, String>? = lastCurrencyPair

        override suspend fun saveLastCurrencyPair(sourceName: String, targetName: String) {
            lastCurrencyPair = sourceName to targetName
        }

        override suspend fun getLastTrackedAmountCurrency(): String? = trackedAmountCurrency

        override suspend fun saveLastTrackedAmountCurrency(isoName: String) {
            trackedAmountCurrency = isoName
        }
    }

    private class FakeRatesRepository : RatesRepository {
        var jpyRate: BigDecimal = BigDecimal("160")

        override suspend fun getRates(): CurrencyRates = CurrencyRates.fromMap(
            rates = mapOf(
                "EUR" to BigDecimal.ONE,
                "JPY" to jpyRate
            ),
            updatedAt = Instant.parse("2026-08-10T00:00:00Z")
        )
    }

    private class FakeExpressionHistoryStore : ExpressionHistoryStore {
        override suspend fun readEntries(): List<HistoryEntry> = emptyList()
        override suspend fun writeEntries(entries: List<HistoryEntry>) = Unit
        override suspend fun clear() = Unit
    }

    private class FakeTrackedAmountStore : TrackedAmountStore {
        private var amounts: List<TrackedAmount> = emptyList()
        private var dayRates: Map<LocalDate, DayRatesSnapshot> = emptyMap()

        fun storedAmounts(): List<TrackedAmount> = amounts
        fun storedDayRates(): Map<LocalDate, DayRatesSnapshot> = dayRates

        override suspend fun readAmounts(): List<TrackedAmount> = amounts
        override suspend fun writeAmounts(amounts: List<TrackedAmount>) {
            this.amounts = amounts
        }

        override suspend fun readDayRates(): Map<LocalDate, DayRatesSnapshot> = dayRates
        override suspend fun writeDayRates(snapshots: Map<LocalDate, DayRatesSnapshot>) {
            dayRates = snapshots
        }

        override suspend fun clear() {
            amounts = emptyList()
            dayRates = emptyMap()
        }
    }

    private class FixedClock(private val now: Instant) : Clock {
        override fun now(): Instant = now
    }
}
