package com.example.calc.ui.screen.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.calc.domain.history.HistoryDayGroup
import kotlinx.datetime.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDrawer(
    historyGroups: List<HistoryDayGroup>,
    onEntrySelected: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = historyGroups.flatMap { group ->
        listOf<HistoryRow>(HistoryRow.Header(group.date)) +
            group.entries.map { entry -> HistoryRow.Entry(entry.expression) }
    }

    ModalDrawerSheet(modifier = modifier) {
        TopAppBar(
            title = {},
            actions = {
                IconButton(onClick = onClearHistory) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Vider l'historique"
                    )
                }
            }
        )
        LazyColumn {
            items(rows) { row ->
                when (row) {
                    is HistoryRow.Entry ->
                        TextButton(
                            onClick = { onEntrySelected(row.expression) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = row.expression,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                    is HistoryRow.Header ->
                        Text(
                            text = formatHistoryDate(row.date),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                }
            }
        }
    }
}

private fun formatHistoryDate(date: LocalDate): String =
    java.time.LocalDate.of(date.year, date.monthNumber, date.dayOfMonth)
        .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

private sealed interface HistoryRow {
    data class Header(val date: LocalDate) : HistoryRow
    data class Entry(val expression: String) : HistoryRow
}
