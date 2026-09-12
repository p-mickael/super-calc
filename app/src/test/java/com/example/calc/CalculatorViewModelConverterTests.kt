package com.example.calc

import com.example.calc.domain.AppPreferenceStore
import com.example.calc.domain.CalculatorMode
import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.conversion.CurrencyRates
import com.example.calc.domain.conversion.RatesRepository
import com.example.calc.domain.history.ExpressionHistoryStore
import com.example.calc.domain.history.HistoryEntry
import com.example.calc.domain.tracking.DayRatesSnapshot
import com.example.calc.domain.tracking.TrackedAmount
import com.example.calc.domain.tracking.TrackedAmountStore
import com.example.calc.ui.screen.CalculatorViewModel
import kotlinx.datetime.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import kotlin.time.Clock
import kotlin.time.Instant

class CalculatorViewModelConverterTests {
    @Test
    fun `converter remembers the last currency pair across restarts`() {
        val preferenceStore = FakeAppPreferenceStore()
        val firstSession = createViewModel(preferenceStore = preferenceStore)

        firstSession.onModeChanged(CalculatorMode.CONVERTER)
        firstSession.onConversionSourceChanged("JPY")
        firstSession.onConversionTargetChanged("EUR")

        val secondSession = createViewModel(preferenceStore = preferenceStore)

        assertEquals("JPY", secondSession.state.value.currencyState.sourceName)
        assertEquals("EUR", secondSession.state.value.currencyState.targetName)
    }

    @Test
    fun `swapping units works even when the expression is empty`() {
        val preferenceStore = FakeAppPreferenceStore()
        val viewModel = createViewModel(preferenceStore = preferenceStore)
        viewModel.onModeChanged(CalculatorMode.CONVERTER)

        viewModel.onSwapUnits()

        assertEquals("JPY", viewModel.state.value.currencyState.sourceName)
        assertEquals("EUR", viewModel.state.value.currencyState.targetName)
        assertEquals("JPY" to "EUR", preferenceStore.savedPair)
    }

    @Test
    fun `converter preview is rounded to 2 decimals`() {
        val viewModel = createViewModel()
        viewModel.onModeChanged(CalculatorMode.CONVERTER)

        viewModel.enter("1")

        assertEquals("53.33", viewModel.state.value.preview)
    }

    @Test
    fun `converter input caps typed decimals at 2`() {
        val viewModel = createViewModel()
        viewModel.onModeChanged(CalculatorMode.CONVERTER)

        viewModel.enter("1.239")

        assertEquals("1.23", viewModel.state.value.expression)
    }

    @Test
    fun `double zero caps at 2 decimals in converter mode`() {
        val viewModel = createViewModel()
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("5")
        viewModel.onDot()

        viewModel.onDoubleZero()

        assertEquals("5.00", viewModel.state.value.expression)

        viewModel.onDoubleZero()

        assertEquals("5.00", viewModel.state.value.expression)
    }

    @Test
    fun `double zero is unrestricted in calculator mode`() {
        val viewModel = createViewModel()
        viewModel.onModeChanged(CalculatorMode.CALCULATOR)
        viewModel.enter("5")
        viewModel.onDot()

        viewModel.onDoubleZero()
        viewModel.onDoubleZero()

        assertEquals("5.0000", viewModel.state.value.expression)
    }

    @Test
    fun `toggling sign is reflected in the expression`() {
        val viewModel = createViewModel()
        viewModel.onModeChanged(CalculatorMode.CALCULATOR)
        viewModel.enter("5")

        viewModel.onToggleSign()

        assertEquals("-5", viewModel.state.value.expression)
    }

    @Test
    fun `converter equals rounds the result to 2 decimals`() {
        val viewModel = createViewModel()
        viewModel.onModeChanged(CalculatorMode.CONVERTER)

        viewModel.enter("1")
        viewModel.onOperator(Operator.DIVIDE)
        viewModel.enter("3")
        viewModel.onEquals()

        assertEquals("0.33", viewModel.state.value.expression)
        assertEquals("17.60", viewModel.state.value.preview)
    }

    @Test
    fun `swapping units pads the amount to 2 decimals`() {
        val viewModel = createViewModel()
        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("5")

        viewModel.onSwapUnits()

        assertEquals("266.67", viewModel.state.value.expression)
        assertEquals("5.00", viewModel.state.value.preview)
    }

    @Test
    fun `calculator preview keeps full precision`() {
        val viewModel = createViewModel()
        viewModel.onModeChanged(CalculatorMode.CALCULATOR)

        viewModel.enter("1")
        viewModel.onOperator(Operator.DIVIDE)
        viewModel.enter("3")

        assertEquals("0.3333333333333333", viewModel.state.value.preview)
    }

    private fun createViewModel(
        preferenceStore: FakeAppPreferenceStore = FakeAppPreferenceStore(),
        now: Instant = Instant.parse("2026-08-10T10:00:00Z")
    ): CalculatorViewModel =
        CalculatorViewModel(
            repository = FakeRatesRepository(),
            appPreferenceStore = preferenceStore,
            expressionHistoryStore = FakeExpressionHistoryStore(),
            trackedAmountStore = FakeTrackedAmountStore(),
            clock = FixedClock(now),
            timeZoneProvider = { kotlinx.datetime.TimeZone.of("Europe/Paris") },
            externalScope = CoroutineScope(Dispatchers.Unconfined)
        )

    private fun CalculatorViewModel.enter(keys: String) {
        keys.forEach { key ->
            when (key) {
                in '0'..'9' -> onDigit(key)
                '.', ',' -> onDot()
                '+' -> onOperator(Operator.PLUS)
                '-', '–' -> onOperator(Operator.MINUS)
                '*', '×' -> onOperator(Operator.TIMES)
                '/', '÷' -> onOperator(Operator.DIVIDE)
                '(' -> onOpenParenthesis()
                ')' -> onCloseParenthesis()
                '%' -> onPercent()
                else -> error("Unknown key: $key")
            }
        }
    }

    private class FakeAppPreferenceStore : AppPreferenceStore {
        private var lastCalculatorMode: CalculatorMode? = null
        var savedPair: Pair<String, String>? = null
            private set

        override suspend fun getLastCalculatorMode(): CalculatorMode? = lastCalculatorMode

        override suspend fun saveLastCalculatorMode(calculatorMode: CalculatorMode) {
            lastCalculatorMode = calculatorMode
        }

        override suspend fun getLastCurrencyPair(): Pair<String, String>? = savedPair

        override suspend fun saveLastCurrencyPair(sourceName: String, targetName: String) {
            savedPair = sourceName to targetName
        }

        private var trackedAmountCurrency: String? = null

        override suspend fun getLastTrackedAmountCurrency(): String? = trackedAmountCurrency

        override suspend fun saveLastTrackedAmountCurrency(isoName: String) {
            trackedAmountCurrency = isoName
        }
    }

    private class FakeRatesRepository : RatesRepository {
        override suspend fun getRates(): CurrencyRates = CurrencyRates.fromMap(
            rates = mapOf(
                "EUR" to BigDecimal("3"),
                "JPY" to BigDecimal("160")
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
