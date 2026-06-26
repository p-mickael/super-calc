package com.example.calc.ui.screen.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import com.example.calc.ui.theme.CalcTheme

@Composable
fun UnitDropdown(
    options: Set<String>,
    selected: String,
    onSelect: (String) -> Unit,
    topOptions: Set<String> = emptySet()
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(selected, color = MaterialTheme.colorScheme.onSurfaceVariant)

        IconButton(onClick = { menuExpanded = true }) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Currencies",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false }
        ) {
            if (topOptions.isNotEmpty()) {
                topOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = {
                            onSelect(option)
                            menuExpanded = false
                        }
                    )
                }
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            options.filter { it !in topOptions }.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = {
                        onSelect(option)
                        menuExpanded = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun UnitDropdownPreview() {
    CalcTheme {
        UnitDropdown(
            setOf("EUR", "JPY"),
            "EUR", {}
        )
    }
}

@Preview
@Composable
fun CurrencyDropdownPreviewExpanded() {
    CalcTheme {
        Column {
            listOf("EUR", "JPY", "USD").forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {}
                )
            }
        }
    }
}