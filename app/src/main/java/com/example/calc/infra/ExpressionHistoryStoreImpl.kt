package com.example.calc.infra

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.calc.domain.calculator.model.Token
import com.example.calc.domain.history.ExpressionHistoryStore
import com.example.calc.domain.history.HistoryEntry
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.time.Instant
import com.example.calc.domain.calculator.model.Operator as DomainOperator

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
        val tokens: List<StoredToken>,
        val recordedAt: String
    ) {
        fun toDomain(): HistoryEntry =
            HistoryEntry(
                tokens = tokens.map { it.toDomain() },
                recordedAt = Instant.parse(recordedAt)
            )

        companion object {
            fun fromDomain(entry: HistoryEntry): StoredHistoryEntry =
                StoredHistoryEntry(
                    tokens = entry.tokens.map { StoredToken.from(it) },
                    recordedAt = entry.recordedAt.toString()
                )
        }
    }

    @Serializable
    private sealed interface StoredToken {
        fun toDomain(): Token

        companion object {
            fun from(token: Token): StoredToken = when (token) {
                is Token.Number -> Number(token.text)
                is Token.Operator -> Operator(token.operator.name)
                Token.Negative -> Negative
                Token.LeftParenthesis -> LeftParenthesis
                Token.RightParenthesis -> RightParenthesis
                Token.Percent -> Percent
            }
        }

        @Serializable
        data class Number(val text: String) : StoredToken {
            override fun toDomain() = Token.Number(text)
        }

        @Serializable
        data class Operator(val operator: String) : StoredToken {
            override fun toDomain() = Token.Operator(DomainOperator.valueOf(operator))
        }

        @Serializable
        data object Negative : StoredToken {
            override fun toDomain() = Token.Negative
        }

        @Serializable
        data object LeftParenthesis : StoredToken {
            override fun toDomain() = Token.LeftParenthesis
        }

        @Serializable
        data object RightParenthesis : StoredToken {
            override fun toDomain() = Token.RightParenthesis
        }

        @Serializable
        data object Percent : StoredToken {
            override fun toDomain() = Token.Percent
        }
    }
}
