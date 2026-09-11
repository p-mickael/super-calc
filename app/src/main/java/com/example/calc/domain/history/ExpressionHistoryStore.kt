package com.example.calc.domain.history

interface ExpressionHistoryStore {
    suspend fun readEntries(): List<HistoryEntry>
    suspend fun writeEntries(entries: List<HistoryEntry>)
    suspend fun clear()
}
