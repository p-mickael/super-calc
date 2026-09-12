package com.example.calc.ui.screen.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.calc.domain.tracking.ConvertedDayGroup
import com.example.calc.ui.screen.model.toFormattedString
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackedAmountsDrawer(
    groups: List<ConvertedDayGroup>,
    displayCurrency: String,
    currencyList: Set<String>,
    onCurrencyChanged: (String) -> Unit,
    onAmountDeleted: (String) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = groups.flatMap { group ->
        listOf<TrackedRow>(TrackedRow.Header(group.date, group.total)) +
            group.amounts.map { amount -> TrackedRow.Amount(amount.trackedAmountId, amount.amount) }
    }

    ModalDrawerSheet(modifier = modifier) {
        TopAppBar(
            title = {
                UnitDropdown(options = currencyList, selected = displayCurrency, onSelect = onCurrencyChanged)
            },
            actions = {
                IconButton(onClick = onClearAll) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Tout supprimer"
                    )
                }
            }
        )
        LazyColumn {
            items(
                rows,
                key = { row ->
                    when (row) {
                        is TrackedRow.Header -> "header:${row.date}"
                        is TrackedRow.Amount -> row.id
                    }
                }
            ) { row ->
                when (row) {
                    is TrackedRow.Header ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatHistoryDate(row.date),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatTrackedAmount(row.total, displayCurrency),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                    is TrackedRow.Amount ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = formatTrackedAmount(row.amount, displayCurrency),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { onAmountDeleted(row.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Supprimer"
                                )
                            }
                        }
                }
            }
        }
    }
}

private fun formatTrackedAmount(value: BigDecimal?, currency: String): String =
    if (value == null) "—" else "${value.toFormattedString(maxDecimals = 2)} $currency"

private sealed interface TrackedRow {
    data class Header(val date: LocalDate, val total: BigDecimal?) : TrackedRow
    data class Amount(val id: String, val amount: BigDecimal?) : TrackedRow
}
