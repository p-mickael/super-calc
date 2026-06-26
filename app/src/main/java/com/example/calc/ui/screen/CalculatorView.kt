package com.example.calc.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.calc.domain.CalculatorMode
import com.example.calc.ui.screen.component.CalculatorDisplay
import com.example.calc.ui.screen.component.ConverterDisplay
import com.example.calc.ui.screen.component.Keyboard
import com.example.calc.ui.screen.component.TopMenu
import com.example.calc.ui.screen.model.CalculatorActions
import com.example.calc.ui.screen.model.UiState
import com.example.calc.ui.theme.CalcTheme
import java.math.BigDecimal

@Composable
fun CalculatorView(viewModel: CalculatorViewModel) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    LifecycleStartEffect(Unit) {
        viewModel.onForeground()
        onStopOrDispose { }
    }

    Content(
        state.value,
        CalculatorActions(
            onDigit = viewModel::onDigit,
            onOperator = viewModel::onOperator,
            onOpenParenthesis = viewModel::onOpenParenthesis,
            onCloseParenthesis = viewModel::onCloseParenthesis,
            onPercent = viewModel::onPercent,
            onDot = viewModel::onDot,
            onClear = viewModel::onClear,
            onDelete = viewModel::onDelete,
            onEquals = viewModel::onEquals,
            onModeChanged = viewModel::onModeChanged,
            onConversionSourceChanged = viewModel::onConversionSourceChanged,
            onConversionTargetChanged = viewModel::onConversionTargetChanged,
            onSwapUnits = viewModel::onSwapUnits,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: UiState,
    actions: CalculatorActions
) {
    Scaffold(
        modifier = Modifier,
        topBar = {
            TopMenu(
                state.calculatorMode,
                actions.onModeChanged
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            when (state.calculatorMode) {
                CalculatorMode.CALCULATOR ->
                    CalculatorDisplay(
                        state.expression,
                        state.preview,
                        Modifier
                            .weight(10f)
                            .fillMaxWidth(),
                    )

                CalculatorMode.CONVERTER ->
                    ConverterDisplay(
                        expression = state.expression,
                        preview = state.preview,
                        selectedSource = state.currencyState.sourceName,
                        selectedTarget = state.currencyState.targetName,
                        unitList = state.currencyState.currencyList,
                        onSourceChanged = actions.onConversionSourceChanged,
                        onTargetChanged = actions.onConversionTargetChanged,
                        onSwapUnits = actions.onSwapUnits,
                        Modifier
                            .weight(10f)
                            .fillMaxWidth(),
                    )
            }

            Keyboard(
                actions,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .weight(20f)
            )
        }
    }
}

@Preview
@Composable
fun CalculatorViewPreview() {
    CalcTheme {
        Content(
            state = UiState(
                expression = "5+4-8x(2+4)",
                previewValue = BigDecimal.ONE,
                calculatorMode = CalculatorMode.CONVERTER
            ), CalculatorActions(
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {},
                {}
            )
        )
    }
}