package com.example.calc.ui.screen.component.keyboardcomponents

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CalculatorButtonInfo(
    val label: String,
    val onPress: () -> Unit = {},
    val onLongPress: () -> Unit = {},
    val color: Color? = null,
    val textColor: Color? = null,
    val icon: ImageVector? = null,
    val enabled: Boolean = true
)

@Composable
fun CalculatorButton(
    buttonInfo: CalculatorButtonInfo,
    modifier: Modifier = Modifier,
) {
    val fontSize = 40.sp
    Surface(
        modifier = modifier
            .alpha(if (buttonInfo.enabled) 1f else 0.38f)
            .combinedClickable(
                enabled = buttonInfo.enabled,
                onClick = buttonInfo.onPress,
                onLongClick = buttonInfo.onLongPress
            ),
        shape = CircleShape,
        color = buttonInfo.color ?: MaterialTheme.colorScheme.primary,
        tonalElevation = 4.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (buttonInfo.icon != null) {
                Icon(
                    imageVector = buttonInfo.icon,
                    contentDescription = buttonInfo.label,
                    tint = buttonInfo.textColor ?: MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(28.dp)
                )
            } else {
                Text(
                    text = buttonInfo.label,
                    fontSize = fontSize,
                    color = buttonInfo.textColor ?: MaterialTheme.colorScheme.onPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Preview
@Composable
fun ButtonPreview() {
    CalculatorButton(
        CalculatorButtonInfo("2")
    )
}
