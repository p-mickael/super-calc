package com.example.calc.ui.screen.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.calc.domain.calculator.model.Operator
import com.example.calc.ui.screen.component.keyboardcomponents.CalculatorButtonInfo
import com.example.calc.ui.screen.component.keyboardcomponents.KeyboardRow
import com.example.calc.ui.screen.model.CalculatorActions
import com.example.calc.ui.theme.CalcTheme

private const val COLUMN_COUNT = 4
private const val ROW_COUNT = 6

@Composable
fun Keyboard(
    actions: CalculatorActions,
    modifier: Modifier = Modifier
) = BoxWithConstraints(modifier = modifier) {
    val gap = 8.dp
    val buttonHeight = (maxHeight - gap * (ROW_COUNT - 1)) / ROW_COUNT
    val buttonWidth = (maxWidth - gap * (COLUMN_COUNT - 1)) / COLUMN_COUNT

    Column(
        verticalArrangement = Arrangement.spacedBy(gap, Alignment.CenterVertically)
    ) {
        KeyboardRow(
            listOf(
                null,
                CalculatorButtonInfo(
                    "+/-",
                    { actions.onToggleSign() },
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                ),
                CalculatorButtonInfo(
                    "AC",
                    { actions.onClear() },
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                ),
                CalculatorButtonInfo(
                    "⌫",
                    { actions.onDelete() },
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                ),
            ),
            buttonWidth,
            buttonHeight
        )
        KeyboardRow(
            listOf(
                CalculatorButtonInfo(
                    "( ",
                    { actions.onOpenParenthesis() },
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                ),
                CalculatorButtonInfo(
                    " )",
                    { actions.onCloseParenthesis() },
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                ),
                CalculatorButtonInfo(
                    "%",
                    { actions.onPercent() },
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                ),
                CalculatorButtonInfo(
                    "÷",
                    { actions.onOperator(Operator.DIVIDE) },
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                ),
            ),
            buttonWidth,
            buttonHeight
        )
        KeyboardRow(
            listOf(
                CalculatorButtonInfo(
                    "7",
                    { actions.onDigit('7') },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "8",
                    { actions.onDigit('8') },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "9",
                    { actions.onDigit('9') },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "×",
                    { actions.onOperator(Operator.TIMES) },
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                ),
            ),
            buttonWidth,
            buttonHeight
        )
        KeyboardRow(
            listOf(
                CalculatorButtonInfo(
                    "4",
                    { actions.onDigit('4') },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "5",
                    { actions.onDigit('5') },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "6",
                    { actions.onDigit('6') },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "–",
                    { actions.onOperator(Operator.MINUS) },
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                ),
            ),
            buttonWidth,
            buttonHeight
        )
        KeyboardRow(
            listOf(
                CalculatorButtonInfo(
                    "1",
                    { actions.onDigit('1') },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "2",
                    { actions.onDigit('2') },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "3",
                    { actions.onDigit('3') },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "+",
                    { actions.onOperator(Operator.PLUS) },
                    color = MaterialTheme.colorScheme.secondary,
                    textColor = MaterialTheme.colorScheme.onSecondary
                ),
            ),
            buttonWidth,
            buttonHeight
        )
        KeyboardRow(
            listOf(
                CalculatorButtonInfo(
                    "0",
                    { actions.onDigit('0') },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    ".",
                    { actions.onDot() },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "00",
                    { actions.onDoubleZero() },
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                CalculatorButtonInfo(
                    "=",
                    { actions.onEquals() },
                    color = MaterialTheme.colorScheme.primary,
                    textColor = MaterialTheme.colorScheme.onPrimary

                ),
            ),
            buttonWidth,
            buttonHeight
        )
    }
}


@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun KeyboardPreview() {
    CalcTheme {
        Keyboard(
            CalculatorActions(
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
            {},
            {},
            {},
            {},
            {},
            {}
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(470.dp)
        )
    }
}