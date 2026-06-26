package com.example.calc.ui.screen.model

import com.example.calc.domain.CalculatorMode
import com.example.calc.domain.calculator.CalculatorInput
import java.math.BigDecimal

data class UiState(
    val input: CalculatorInput = CalculatorInput.EMPTY,
    val expression: String = "",
    val previewValue: BigDecimal? = null,
    val expressionState: ExpressionState = ExpressionState.EDITING,
    val calculatorMode: CalculatorMode = CalculatorMode.CALCULATOR,
    val currencyState: CurrencyState = CurrencyState()
) {
    val preview: String get() = previewValue.toFormattedString()
}

data class CurrencyState(
    val sourceName: String = "EUR",
    val targetName: String = "JPY",
    val currencyList: Set<String> = setOf("AUD", "EUR", "JPY")
)