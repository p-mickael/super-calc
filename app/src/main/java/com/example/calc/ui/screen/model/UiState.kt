package com.example.calc.ui.screen.model

import com.example.calc.domain.CalculatorMode
import com.example.calc.domain.calculator.CalculatorInput
import com.example.calc.domain.conversion.EURO_ISO
import com.example.calc.domain.history.HistoryDayGroup
import com.example.calc.domain.tracking.ConvertedDayGroup
import java.math.BigDecimal

data class UiState(
    val input: CalculatorInput = CalculatorInput.EMPTY,
    val expression: String = "",
    val previewValue: BigDecimal? = null,
    val expressionState: ExpressionState = ExpressionState.EDITING,
    val calculatorMode: CalculatorMode = CalculatorMode.CONVERTER,
    val currencyState: CurrencyState = CurrencyState(),
    val historyGroups: List<HistoryDayGroup> = emptyList(),
    val trackedAmounts: TrackedAmountsState = TrackedAmountsState(),
    val expressionFocusRequestKey: Int = 0
) {
    val preview: String get() = previewValue.toFormattedString(
        maxDecimals = if (calculatorMode == CalculatorMode.CONVERTER) 2 else null
    )

    val canSaveAmount: Boolean
        get() = calculatorMode == CalculatorMode.CONVERTER && previewValue != null
}

data class CurrencyState(
    val sourceName: String = "EUR",
    val targetName: String = "JPY",
    val currencyList: Set<String> = setOf("AUD", "EUR", "JPY")
)

data class TrackedAmountsState(
    val displayCurrency: String = EURO_ISO,
    val groups: List<ConvertedDayGroup> = emptyList()
)