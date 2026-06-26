package com.example.calc.ui.screen.component.keyboardcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun KeyboardRow(buttonsInfo: Iterable<CalculatorButtonInfo>) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        buttonsInfo.forEach {
            CalculatorButton(
                it,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
            )
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
        )
    )
}