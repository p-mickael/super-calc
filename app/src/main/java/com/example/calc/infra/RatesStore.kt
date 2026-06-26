package com.example.calc.infra

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.calc.domain.conversion.CurrencyRates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.time.Instant

class RatesStore(private val dataStore: DataStore<Preferences>) {
    companion object {
        val RATES_KEY = stringPreferencesKey("rates")
        val UPDATED_AT = stringPreferencesKey("updated_at")
    }

    fun read(): Flow<CurrencyRates?> {
        return dataStore.data.map {
            val ratesJson = (it[RATES_KEY]) ?: return@map null
            val updatedAt = it[UPDATED_AT] ?: return@map null
            CurrencyRates.fromMap(
                rates = Json.decodeFromString<Map<String, String>>(ratesJson)
                    .mapValues { (_, value) -> value.toBigDecimal() },
                updatedAt = Instant.parse(updatedAt)
            )
        }
    }

    suspend fun write(rates: CurrencyRates) = dataStore.edit {
        it[RATES_KEY] = Json.encodeToString(
            rates.ratesMap.mapValues { (_, value) -> value.toPlainString() })
        it[UPDATED_AT] = rates.updatedAt.toString()
    }
}