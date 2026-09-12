package com.example.calc.ui.screen.model

import com.example.calc.domain.CalculatorMode
import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.calculator.model.Token

data class CalculatorActions(
    val onDigit: (Char) -> Unit,
    val onOperator: (Operator) -> Unit,
    val onOpenParenthesis: () -> Unit,
    val onCloseParenthesis: () -> Unit,
    val onPercent: () -> Unit,
    val onDot: () -> Unit,
    val onToggleSign: () -> Unit,
    val onDoubleZero: () -> Unit,
    val onClear: () -> Unit,
    val onDelete: () -> Unit,
    val onEquals: () -> Unit,
    val onModeChanged: (CalculatorMode) -> Unit,
    val onConversionSourceChanged: (String) -> Unit,
    val onConversionTargetChanged: (String) -> Unit,
    val onSwapUnits: () -> Unit,
    val onHistoryRequested: () -> Unit,
    val onClearHistory: () -> Unit,
    val onHistoryEntrySelected: (List<Token>) -> Unit,
    val onSaveAmount: () -> Unit,
    val onTrackedAmountsRequested: () -> Unit,
    val onTrackedAmountDeleted: (String) -> Unit,
    val onClearTrackedAmounts: () -> Unit,
    val onTrackedAmountCurrencyChanged: (String) -> Unit
)