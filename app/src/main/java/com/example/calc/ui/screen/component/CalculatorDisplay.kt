package com.example.calc.ui.screen.component

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calc.ui.theme.CalcTheme

@Composable
fun CalculatorDisplay(
    expression: String,
    preview: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier)
    {
        Text(
            expression,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            autoSize = TextAutoSize.StepBased(minFontSize = 24.sp),
            textAlign = TextAlign.Right,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
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
                .padding(8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun CalculatorDisplayPreview() {
    CalcTheme {
        CalculatorDisplay(
            "5+3x2-5",
            "6"
        )
    }
}