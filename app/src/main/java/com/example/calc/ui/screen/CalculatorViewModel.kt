package com.example.calc.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.calc.domain.AppPreferenceStore
import com.example.calc.domain.CalculatorMode
import com.example.calc.domain.calculator.Calculator
import com.example.calc.domain.calculator.CalculatorInput
import com.example.calc.domain.calculator.CalculatorResult
import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.calculator.model.Token
import com.example.calc.domain.conversion.CurrencyConverter
import com.example.calc.domain.conversion.RatesRepository
import com.example.calc.domain.calculator.renderExpression
import com.example.calc.domain.history.ExpressionHistory
import com.example.calc.domain.history.ExpressionHistoryStore
import com.example.calc.domain.history.HistoryEntry
import com.example.calc.ui.screen.model.ExpressionState
import com.example.calc.ui.screen.model.UiState
import com.example.calc.ui.screen.model.toFormattedString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.TimeZone
import java.math.BigDecimal
import kotlin.time.Clock

class CalculatorViewModel(
    private val repository: RatesRepository,
    private val appPreferenceStore: AppPreferenceStore,
    private val expressionHistoryStore: ExpressionHistoryStore,
    private val clock: Clock = Clock.System,
    private val timeZoneProvider: () -> TimeZone = { TimeZone.currentSystemDefault() },
    private val externalScope: CoroutineScope? = null
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    private val calculator = Calculator()
    private var currencyConverter: CurrencyConverter? = null
    private val historyMutex = Mutex()
    private val scope: CoroutineScope get() = externalScope ?: viewModelScope

    companion object {
        private const val MAX_HISTORY_ENTRIES = 500
    }

    init {
        scope.launch {
            appPreferenceStore.getLastCurrencyPair()?.let { (sourceName, targetName) ->
                _state.update {
                    it.copy(currencyState = it.currencyState.copy(sourceName = sourceName, targetName = targetName))
                }
            }

            appPreferenceStore.getLastCalculatorMode()?.let { lastCalculatorMode ->
                if (lastCalculatorMode == CalculatorMode.CONVERTER)
                    switchToConverter()
            }
        }
    }

    fun onDigit(char: Char) = edit { currentState ->
        val inputToUpdate = when (currentState.expressionState) {
            ExpressionState.EDITING -> currentState.input
            else -> currentState.input.clear()
        }

        inputToUpdate.appendDigit(char)
    }

    fun onOperator(operator: Operator) = edit { it.input.appendOperator(operator) }
    fun onPercent() = edit { it.input.appendPercent() }
    fun onOpenParenthesis() = edit { it.input.openParenthesis() }
    fun onCloseParenthesis() = edit { it.input.closeParenthesis() }
    fun onDot() = edit { it.input.appendDot() }
    fun onDelete() = edit { currentState ->
        if (currentState.expressionState != ExpressionState.EDITING)
            currentState.input.clear()
        else
            currentState.input.deleteLast()
    }

    fun onClear() = edit {
        it.input.clear()
    }

    fun onEquals() {
        val currentState = _state.value

        when (val result = calculator.calculate(currentState.input)) {
            is CalculatorResult.Value -> {
                val previewValue = if (currentState.calculatorMode == CalculatorMode.CALCULATOR) null
                else displayValueOf(currentState.calculatorMode, result.value)

                _state.update {
                    it.copy(
                        input = CalculatorInput.of(result.value),
                        expression = result.value.toFormattedString(),
                        previewValue = previewValue,
                        expressionState = ExpressionState.RESULT
                    )
                }
                scope.launch {
                    saveHistoryEntry(currentState.input.tokens)
                }
            }

            CalculatorResult.DivisionByZero ->
                _state.update {
                    it.copy(
                        expression = "Division par zéro",
                        previewValue = null,
                        expressionState = ExpressionState.ERROR
                    )
                }

            CalculatorResult.Incomplete -> Unit
        }
    }

    fun onModeChanged(newMode: CalculatorMode) = scope.launch {
        when (newMode) {
            CalculatorMode.CONVERTER -> switchToConverter()
            CalculatorMode.CALCULATOR -> switchToCalculator()
        }

        appPreferenceStore.saveLastCalculatorMode(_state.value.calculatorMode)
    }

    fun onConversionSourceChanged(newSource: String) = scope.launch {
        val targetName = _state.value.currencyState.targetName
        if (updateConverter(newSource, targetName)) {
            appPreferenceStore.saveLastCurrencyPair(newSource, targetName)
        }
    }

    fun onConversionTargetChanged(newTarget: String) = scope.launch {
        val sourceName = _state.value.currencyState.sourceName
        if (updateConverter(sourceName, newTarget)) {
            appPreferenceStore.saveLastCurrencyPair(sourceName, newTarget)
        }
    }

    fun onSwapUnits() = scope.launch {
        val current = _state.value.currencyState
        val previewValue = _state.value.previewValue

        if (!updateConverter(current.targetName, current.sourceName)) return@launch

        appPreferenceStore.saveLastCurrencyPair(current.targetName, current.sourceName)

        if (previewValue != null) {
            val nextInput = CalculatorInput.of(previewValue)
            _state.update {
                it.copy(
                    input = nextInput,
                    expression = nextInput.tokens.renderExpression(),
                    previewValue = computePreviewValue(CalculatorMode.CONVERTER, nextInput)
                )
            }
        }
    }

    fun onHistoryRequested() = scope.launch {
        refreshHistory()
    }

    fun onClearHistory() = scope.launch {
        historyMutex.withLock {
            expressionHistoryStore.clear()
            _state.update { it.copy(historyGroups = emptyList()) }
        }
    }

    fun onHistoryEntrySelected(tokens: List<Token>) {
        val restoredInput = CalculatorInput.fromTokens(tokens) ?: return

        _state.update {
            it.copy(
                input = restoredInput,
                expression = restoredInput.tokens.renderExpression(),
                previewValue = computePreviewValue(it.calculatorMode, restoredInput),
                expressionState = ExpressionState.EDITING,
                expressionFocusRequestKey = it.expressionFocusRequestKey + 1
            )
        }
    }

    fun onForeground() {
        if (_state.value.calculatorMode == CalculatorMode.CONVERTER)
            scope.launch {
                updateConverter(
                    _state.value.currencyState.sourceName,
                    _state.value.currencyState.targetName
                )
            }
    }

    private suspend fun updateConverter(sourceName: String, targetName: String): Boolean {
        val rates = repository.getRates()
        if (rates == null) {
            switchToCalculator()
            return false
        }

        currencyConverter = CurrencyConverter(
            rates = rates,
            sourceName,
            targetName
        )
        _state.update {
            it.copy(
                currencyState = it.currencyState.copy(
                    sourceName = sourceName,
                    targetName = targetName,
                    currencyList = rates.ratesMap.keys
                ),
                previewValue = computePreviewValue(CalculatorMode.CONVERTER, it.input)
            )
        }

        return true
    }

    private suspend fun switchToConverter() {
        if (updateConverter(
                _state.value.currencyState.sourceName,
                _state.value.currencyState.targetName
            )
        ) {
            _state.update {
                it.copy(
                    calculatorMode = CalculatorMode.CONVERTER,
                )
            }
        }
    }

    private fun switchToCalculator() {
        _state.update {
            val newMode = CalculatorMode.CALCULATOR
            it.copy(
                calculatorMode = newMode,
                previewValue = computePreviewValue(newMode, it.input),
            )
        }
    }

    private fun edit(operation: (UiState) -> CalculatorInput) = _state.update {
        val nextInput = operation(it)
        it.copy(
            input = nextInput,
            expression = nextInput.tokens.renderExpression(),
            previewValue = computePreviewValue(it.calculatorMode, nextInput),
            expressionState = ExpressionState.EDITING
        )
    }

    private fun computePreviewValue(
        calculatorMode: CalculatorMode,
        calculatorInput: CalculatorInput
    ): BigDecimal? = displayValueOf(
        calculatorMode, calculator.preview(calculatorInput)
    )

    private fun displayValueOf(
        calculatorMode: CalculatorMode,
        valueToCompute: BigDecimal?
    ): BigDecimal? =
        valueToCompute?.let { value ->
            when (calculatorMode) {
                CalculatorMode.CALCULATOR -> value
                CalculatorMode.CONVERTER -> currencyConverter?.convert(value)
            }
        }

    private suspend fun refreshHistory() {
        val timeZone = timeZoneProvider()
        historyMutex.withLock {
            val retainedEntries = ExpressionHistory.retainLast30Days(
                expressionHistoryStore.readEntries(),
                clock.now(),
                timeZone
            )

            expressionHistoryStore.writeEntries(retainedEntries)
            _state.update {
                it.copy(
                    historyGroups = ExpressionHistory.groupByDay(
                        retainedEntries,
                        timeZone
                    )
                )
            }
        }
    }

    private suspend fun saveHistoryEntry(tokens: List<Token>) {
        historyMutex.withLock {
            val existingEntries = expressionHistoryStore.readEntries()
            if (existingEntries.firstOrNull()?.tokens == tokens) return@withLock

            val newEntry = HistoryEntry(tokens, clock.now())
            expressionHistoryStore.writeEntries(
                (listOf(newEntry) + existingEntries).take(MAX_HISTORY_ENTRIES)
            )
        }
    }
}