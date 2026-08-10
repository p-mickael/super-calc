package com.example.calc

import com.example.calc.domain.AppPreferenceStore
import com.example.calc.domain.CalculatorMode
import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.calculator.model.Token
import com.example.calc.domain.conversion.CurrencyRates
import com.example.calc.domain.conversion.RatesRepository
import com.example.calc.domain.history.ExpressionHistoryStore
import com.example.calc.domain.history.HistoryEntry
import com.example.calc.ui.screen.CalculatorViewModel
import com.example.calc.ui.screen.model.ExpressionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import kotlin.time.Clock
import kotlin.time.Instant

class CalculatorViewModelHistoryTests {
    @Test
    fun `successful equals saves the validated source expression`() {
        val historyStore = FakeExpressionHistoryStore()
        val viewModel = createViewModel(historyStore = historyStore)

        viewModel.enter("1+2")
        viewModel.onEquals()

        assertEquals(listOf("1+2"), historyStore.entries.map { it.expression })
        assertEquals("3", viewModel.state.value.expression)
    }

    @Test
    fun `division by zero is never saved in history`() {
        val historyStore = FakeExpressionHistoryStore()
        val viewModel = createViewModel(historyStore = historyStore)

        viewModel.enter("8÷0")
        viewModel.onEquals()

        assertTrue(historyStore.entries.isEmpty())
        assertEquals(ExpressionState.ERROR, viewModel.state.value.expressionState)
    }

    @Test
    fun `converter equals also saves the source expression`() {
        val historyStore = FakeExpressionHistoryStore()
        val viewModel = createViewModel(historyStore = historyStore)

        viewModel.onModeChanged(CalculatorMode.CONVERTER)
        viewModel.enter("1+2")
        viewModel.onEquals()

        assertEquals(CalculatorMode.CONVERTER, viewModel.state.value.calculatorMode)
        assertEquals(listOf("1+2"), historyStore.entries.map { it.expression })
        assertEquals("3", viewModel.state.value.expression)
    }

    @Test
    fun `opening history cleans expired entries and groups by local day`() {
        val historyStore = FakeExpressionHistoryStore(
            listOf(
                HistoryEntry("expired", Instant.parse("2026-07-11T21:59:59Z")),
                HistoryEntry("boundary", Instant.parse("2026-07-11T22:00:00Z")),
                HistoryEntry("today", Instant.parse("2026-08-10T00:30:00Z"))
            )
        )
        val viewModel = createViewModel(
            historyStore = historyStore,
            now = Instant.parse("2026-08-10T01:00:00Z")
        )

        viewModel.onHistoryRequested()

        assertEquals(listOf("today", "boundary"), historyStore.entries.map { it.expression })
        assertEquals(
            listOf("2026-08-10", "2026-07-12"),
            viewModel.state.value.historyGroups.map { it.date.toString() }
        )
    }

    @Test
    fun `selecting a history entry restores the input and requests focus at the end`() {
        val viewModel = createViewModel()

        viewModel.onHistoryEntrySelected("5×(2)")

        assertEquals(
            listOf(
                Token.Number("5"),
                Token.Operator(Operator.TIMES),
                Token.LeftParenthesis,
                Token.Number("2"),
                Token.RightParenthesis
            ),
            viewModel.state.value.input.tokens
        )
        assertEquals("5×(2)", viewModel.state.value.expression)
        assertEquals("10", viewModel.state.value.preview)
        assertEquals(ExpressionState.EDITING, viewModel.state.value.expressionState)
        assertEquals(1, viewModel.state.value.expressionFocusRequestKey)
    }

    @Test
    fun `clear history empties both persistence and visible groups`() {
        val historyStore = FakeExpressionHistoryStore(
            listOf(HistoryEntry("1+2", Instant.parse("2026-08-10T00:30:00Z")))
        )
        val viewModel = createViewModel(historyStore = historyStore)

        viewModel.onHistoryRequested()
        viewModel.onClearHistory()

        assertTrue(historyStore.entries.isEmpty())
        assertTrue(viewModel.state.value.historyGroups.isEmpty())
    }

    private fun createViewModel(
        historyStore: FakeExpressionHistoryStore = FakeExpressionHistoryStore(),
        now: Instant = Instant.parse("2026-08-10T10:00:00Z")
    ): CalculatorViewModel =
        CalculatorViewModel(
            repository = FakeRatesRepository(),
            appPreferenceStore = FakeAppPreferenceStore(),
            expressionHistoryStore = historyStore,
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

        override suspend fun getLastCalculatorMode(): CalculatorMode? = lastCalculatorMode

        override suspend fun saveLastCalculatorMode(calculatorMode: CalculatorMode) {
            lastCalculatorMode = calculatorMode
        }
    }

    private class FakeRatesRepository : RatesRepository {
        override suspend fun getRates(): CurrencyRates = CurrencyRates.fromMap(
            rates = mapOf(
                "EUR" to BigDecimal.ONE,
                "JPY" to BigDecimal("160")
            ),
            updatedAt = Instant.parse("2026-08-10T00:00:00Z")
        )
    }

    private class FakeExpressionHistoryStore(
        initialEntries: List<HistoryEntry> = emptyList()
    ) : ExpressionHistoryStore {
        var entries: List<HistoryEntry> = initialEntries
            private set

        override suspend fun readEntries(): List<HistoryEntry> = entries

        override suspend fun writeEntries(entries: List<HistoryEntry>) {
            this.entries = entries
        }

        override suspend fun clear() {
            entries = emptyList()
        }
    }

    private class FixedClock(private val now: Instant) : Clock {
        override fun now(): Instant = now
    }
}
