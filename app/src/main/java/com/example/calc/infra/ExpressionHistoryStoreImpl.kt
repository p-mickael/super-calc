package com.example.calc.infra

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.remove
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.calc.domain.history.ExpressionHistoryStore
import com.example.calc.domain.history.HistoryEntry
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class ExpressionHistoryStoreImpl(
    private val dataStore: DataStore<Preferences>
) : ExpressionHistoryStore {
    companion object {
        private val KEY = stringPreferencesKey("expression_history_entries")
    }

    override suspend fun readEntries(): List<HistoryEntry> {
        val encodedEntries = dataStore.data.first()[KEY] ?: return emptyList()

        return runCatching {
            Json.decodeFromString<List<StoredHistoryEntry>>(encodedEntries)
                .map { it.toDomain() }
        }.getOrElse {
            clear()
            emptyList()
        }
    }

    override suspend fun writeEntries(entries: List<HistoryEntry>) {
        dataStore.edit { preferences ->
            if (entries.isEmpty()) {
                preferences.remove(KEY)
            } else {
                preferences[KEY] = Json.encodeToString(
                    entries.map(StoredHistoryEntry::fromDomain)
                )
            }
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(KEY)
        }
    }

    @Serializable
    private data class StoredHistoryEntry(
        val expression: String,
        val recordedAt: String
    ) {
        fun toDomain(): HistoryEntry =
            HistoryEntry(
                expression = expression,
                recordedAt = Instant.parse(recordedAt)
            )

        companion object {
            fun fromDomain(entry: HistoryEntry): StoredHistoryEntry =
                StoredHistoryEntry(
                    expression = entry.expression,
                    recordedAt = entry.recordedAt.toString()
                )
        }
    }
}
