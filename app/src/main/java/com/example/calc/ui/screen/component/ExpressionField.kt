package com.example.calc.ui.screen.component

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExpressionField(
    expression: String,
    focusRequestKey: Int,
    textColor: Color,
    modifier: Modifier = Modifier,
    maxFontSize: TextUnit = 112.sp,
    minFontSize: TextUnit = 24.sp
) {
    val focusRequester = remember { FocusRequester() }
    var fieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = expression,
                selection = TextRange(expression.length)
            )
        )
    }

    LaunchedEffect(expression) {
        fieldValue = TextFieldValue(
            text = expression,
            selection = TextRange(expression.length)
        )
    }

    LaunchedEffect(focusRequestKey) {
        if (focusRequestKey > 0) {
            fieldValue = fieldValue.copy(selection = TextRange(fieldValue.text.length))
            focusRequester.requestFocus()
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val fieldPadding = 8.dp

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val paddingPx = with(density) { (fieldPadding * 2).roundToPx() }
        val availableConstraints = Constraints(
            maxWidth = (constraints.maxWidth - paddingPx).coerceAtLeast(0),
            maxHeight = if (constraints.hasBoundedHeight)
                (constraints.maxHeight - paddingPx).coerceAtLeast(0)
            else Constraints.Infinity
        )
        val fontSize = remember(expression, availableConstraints, maxFontSize, minFontSize) {
            fittingFontSize(textMeasurer, expression, availableConstraints, maxFontSize, minFontSize)
        }

        BasicTextField(
            value = fieldValue,
            onValueChange = { fieldValue = it.copy(text = expression) },
            readOnly = true,
            singleLine = true,
            textStyle = TextStyle(
                color = textColor,
                textAlign = TextAlign.Right,
                fontSize = fontSize
            ),
            cursorBrush = SolidColor(textColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(fieldPadding)
                .focusRequester(focusRequester)
        )
    }
}

private fun fittingFontSize(
    textMeasurer: TextMeasurer,
    text: String,
    constraints: Constraints,
    maxFontSize: TextUnit,
    minFontSize: TextUnit
): TextUnit {
    val sample = text.ifEmpty { "0" }
    val availableHeight = if (constraints.hasBoundedHeight) constraints.maxHeight else Int.MAX_VALUE
    var candidate = maxFontSize.value

    while (candidate > minFontSize.value) {
        val measured = textMeasurer.measure(
            text = sample,
            style = TextStyle(fontSize = candidate.sp),
            maxLines = 1
        )
        if (measured.size.width <= constraints.maxWidth && measured.size.height <= availableHeight) {
            return candidate.sp
        }
        candidate -= 2f
    }
    return minFontSize
}
