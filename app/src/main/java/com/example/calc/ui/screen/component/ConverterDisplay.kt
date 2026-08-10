package com.example.calc.ui.screen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.ui.theme.CalcTheme

@Composable
fun ConverterDisplay(
    expression: String,
    preview: String,
    expressionFocusRequestKey: Int,
    selectedSource: String,
    selectedTarget: String,
    unitList: Set<String>,
    onSourceChanged: (String) -> Unit,
    onTargetChanged: (String) -> Unit,
    onSwapUnits: () -> Unit,
    modifier: Modifier = Modifier
) {
    val topUnits = setOf("EUR", "JPY")
    Column(
        horizontalAlignment = Alignment.End,
        modifier = modifier
    ) {
        ExpressionField(
            expression = expression,
            focusRequestKey = expressionFocusRequestKey,
            textColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        UnitDropdown(unitList, selectedSource, onSourceChanged, topUnits)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSwapUnits) {
                Icon(
                    Icons.Default.SwapVert,
                    contentDescription = "Swap Units"
                )
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            preview,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            autoSize = TextAutoSize.StepBased(
                maxFontSize = 60.sp,
                minFontSize = 12.sp
            ),
            textAlign = TextAlign.Right,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .weight(1f),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        UnitDropdown(unitList, selectedTarget, onTargetChanged, topUnits)
    }
}

@Preview
@Composable
fun ConverterDisplayPreview() {
    CalcTheme {
        ConverterDisplay(
            "4+8-7",
            "5",
            0,
            "EUR",
            "JPY",
            setOf("EUR", "JPY", "USD"),
            {}, {}, {}
        )
    }
}