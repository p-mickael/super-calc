package com.example.calc.ui.screen.component

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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ExpressionField(
    expression: String,
    focusRequestKey: Int,
    textColor: Color,
    modifier: Modifier = Modifier
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

    BasicTextField(
        value = fieldValue,
        onValueChange = { fieldValue = it.copy(text = expression) },
        readOnly = true,
        singleLine = true,
        textStyle = TextStyle(
            color = textColor,
            textAlign = TextAlign.Right,
            fontSize = 48.sp
        ),
        cursorBrush = SolidColor(textColor),
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .focusRequester(focusRequester)
    )
}
