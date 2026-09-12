package com.example.calc.ui.screen.component.keyboardcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val KEY_GAP = 8.dp

private fun Dp.spanned(span: Int) = this * span + KEY_GAP * (span - 1)

@Composable
fun KeyboardRow(buttonsInfo: List<CalculatorButtonInfo?>, buttonWidth: Dp, buttonHeight: Dp) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KEY_GAP)
    ) {
        buttonsInfo.forEach {
            if (it != null) {
                CalculatorButton(
                    it,
                    modifier = Modifier.size(buttonWidth.spanned(it.columnSpan), buttonHeight)
                )
            } else {
                Spacer(modifier = Modifier.size(buttonWidth, buttonHeight))
            }
        }
    }
}

@Preview
@Composable
fun KeyboardRowListPreview() {
    KeyboardRow(
        listOf(
            CalculatorButtonInfo("1"),
            CalculatorButtonInfo("2"),
            CalculatorButtonInfo("3"),
            CalculatorButtonInfo("+"),
        ),
        buttonWidth = 72.dp,
        buttonHeight = 64.dp
    )
}