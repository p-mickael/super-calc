package com.example.calc.infra

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.calc.domain.tracking.DayRatesSnapshot
import com.example.calc.domain.tracking.TrackedAmount
import com.example.calc.domain.tracking.TrackedAmountStore
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.math.BigDecimal
import kotlin.time.Instant

class TrackedAmountStoreImpl(
    private val dataStore: DataStore<Preferences>
) : TrackedAmountStore {
    companion object {
        private val AMOUNTS_KEY = stringPreferencesKey("tracked_amounts")
        private val DAY_RATES_KEY = stringPreferencesKey("tracked_amount_day_rates")
    }

    override suspend fun readAmounts(): List<TrackedAmount> {
        val encodedAmounts = dataStore.data.first()[AMOUNTS_KEY] ?: return emptyList()

        return runCatching {
            Json.decodeFromString<List<StoredTrackedAmount>>(encodedAmounts)
                .map { it.toDomain() }
        }.getOrElse {
            clearAmounts()
            emptyList()
        }
    }

    override suspend fun writeAmounts(amounts: List<TrackedAmount>) {
        dataStore.edit { preferences ->
            if (amounts.isEmpty()) {
                preferences.remove(AMOUNTS_KEY)
            } else {
                preferences[AMOUNTS_KEY] = Json.encodeToString(
                    amounts.map(StoredTrackedAmount::fromDomain)
                )
            }
        }
    }

    override suspend fun readDayRates(): Map<LocalDate, DayRatesSnapshot> {
        val encodedSnapshots = dataStore.data.first()[DAY_RATES_KEY] ?: return emptyMap()

        return runCatching {
            Json.decodeFromString<List<StoredDayRates>>(encodedSnapshots)
                .map { it.toDomain() }
                .associateBy { it.date }
        }.getOrElse {
            clearDayRates()
            emptyMap()
        }
    }

    override suspend fun writeDayRates(snapshots: Map<LocalDate, DayRatesSnapshot>) {
        dataStore.edit { preferences ->
            if (snapshots.isEmpty()) {
                preferences.remove(DAY_RATES_KEY)
            } else {
                preferences[DAY_RATES_KEY] = Json.encodeToString(
                    snapshots.values.map(StoredDayRates::fromDomain)
                )
            }
        }
    }

    override suspend fun clear() {
        clearAmounts()
        clearDayRates()
    }

    private suspend fun clearAmounts() {
        dataStore.edit { preferences -> preferences.remove(AMOUNTS_KEY) }
    }

    private suspend fun clearDayRates() {
        dataStore.edit { preferences -> preferences.remove(DAY_RATES_KEY) }
    }

    @Serializable
    private data class StoredTrackedAmount(
        val id: String,
        val amountEur: String,
        val recordedAt: String
    ) {
        fun toDomain(): TrackedAmount =
            TrackedAmount(
                id = id,
                amountEur = BigDecimal(amountEur),
                recordedAt = Instant.parse(recordedAt)
            )

        companion object {
            fun fromDomain(amount: TrackedAmount): StoredTrackedAmount =
                StoredTrackedAmount(
                    id = amount.id,
                    amountEur = amount.amountEur.toPlainString(),
                    recordedAt = amount.recordedAt.toString()
                )
        }
    }

    @Serializable
    private data class StoredDayRates(
        val date: String,
        val rates: Map<String, String>,
        val capturedAt: String
    ) {
        fun toDomain(): DayRatesSnapshot =
            DayRatesSnapshot(
                date = LocalDate.parse(date),
                ratesMap = rates.mapValues { (_, value) -> BigDecimal(value) },
                capturedAt = Instant.parse(capturedAt)
            )

        companion object {
            fun fromDomain(snapshot: DayRatesSnapshot): StoredDayRates =
                StoredDayRates(
                    date = snapshot.date.toString(),
                    rates = snapshot.ratesMap.mapValues { (_, value) -> value.toPlainString() },
                    capturedAt = snapshot.capturedAt.toString()
                )
        }
    }
}
