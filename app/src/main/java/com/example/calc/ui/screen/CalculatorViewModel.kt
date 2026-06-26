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
import com.example.calc.ui.screen.model.ExpressionState
import com.example.calc.ui.screen.model.UiState
import com.example.calc.ui.screen.model.toFormattedString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CalculatorViewModel(
    private val repository: RatesRepository,
    private val appPreferenceStore: AppPreferenceStore
) : ViewModel() {
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()
    private val calculator = Calculator()
    private var currencyConverter: CurrencyConverter? = null

    init {
        viewModelScope.launch {
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

    fun onEquals() = _state.update { s ->
        when (val result = calculator.calculate(s.input)) {
            is CalculatorResult.Value -> s.copy(
                input = CalculatorInput.of(result.value),
                expression = result.value.toFormattedString(),
                previewValue = if (s.calculatorMode == CalculatorMode.CALCULATOR) null
                else displayValueOf(s.calculatorMode, result.value),
                expressionState = ExpressionState.RESULT
            )

            CalculatorResult.DivisionByZero -> s.copy(
                expression = "Division par zéro",
                previewValue = null,
                expressionState = ExpressionState.ERROR
            )

            CalculatorResult.Incomplete -> s
        }
    }

    fun onModeChanged(newMode: CalculatorMode) = viewModelScope.launch {
        when (newMode) {
            CalculatorMode.CONVERTER -> switchToConverter()
            CalculatorMode.CALCULATOR -> switchToCalculator()
        }

        appPreferenceStore.saveLastCalculatorMode(_state.value.calculatorMode)
    }

    fun onConversionSourceChanged(newSource: String) = viewModelScope.launch {
        updateConverter(newSource, _state.value.currencyState.targetName)
    }

    fun onConversionTargetChanged(newTarget: String) = viewModelScope.launch {
        updateConverter(_state.value.currencyState.sourceName, newTarget)
    }

    fun onSwapUnits() = viewModelScope.launch {
        val current = _state.value.currencyState
        val previewValue = _state.value.previewValue ?: return@launch

        if (updateConverter(current.targetName, current.sourceName)) {
            val nextInput = CalculatorInput.of(previewValue)
            _state.update {
                it.copy(
                    input = nextInput,
                    expression = renderTokens(nextInput),
                    previewValue = computePreviewValue(CalculatorMode.CONVERTER, nextInput)
                )
            }
        }
    }

    fun onForeground() {
        if (_state.value.calculatorMode == CalculatorMode.CONVERTER)
            viewModelScope.launch {
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
            expression = renderTokens(nextInput),
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

    private fun renderTokens(input: CalculatorInput): String =
        input.tokens.joinToString("") { token ->
            when (token) {
                is Token.Number -> token.text
                is Token.Operator -> when (token.operator) {
                    Operator.PLUS -> "+"
                    Operator.MINUS -> "–"
                    Operator.TIMES -> "×"
                    Operator.DIVIDE -> "÷"
                }

                is Token.Percent -> "%"
                is Token.Negative -> "-"
                is Token.LeftParenthesis -> "("
                is Token.RightParenthesis -> ")"
            }
        }
}