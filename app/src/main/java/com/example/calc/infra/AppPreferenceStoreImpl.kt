package com.example.calc.infra

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.calc.domain.AppPreferenceStore
import com.example.calc.domain.CalculatorMode
import kotlinx.coroutines.flow.first

class AppPreferenceStoreImpl(
    private val dataStore: DataStore<Preferences>
) : AppPreferenceStore {

    companion object {
        val KEY = stringPreferencesKey("last_calculator_mode")
        val SOURCE_CURRENCY_KEY = stringPreferencesKey("last_source_currency")
        val TARGET_CURRENCY_KEY = stringPreferencesKey("last_target_currency")
        val TRACKED_AMOUNT_CURRENCY_KEY = stringPreferencesKey("last_tracked_amount_currency")
    }

    override suspend fun getLastCalculatorMode(): CalculatorMode? {
        val modeAsString = dataStore.data.first()[KEY] ?: return null
        return runCatching { CalculatorMode.valueOf(modeAsString) }.getOrNull()
    }

    override suspend fun saveLastCalculatorMode(calculatorMode: CalculatorMode) {
        dataStore.edit {
            it[KEY] = calculatorMode.toString()
        }
    }

    override suspend fun getLastCurrencyPair(): Pair<String, String>? {
        val preferences = dataStore.data.first()
        val sourceName = preferences[SOURCE_CURRENCY_KEY] ?: return null
        val targetName = preferences[TARGET_CURRENCY_KEY] ?: return null
        return sourceName to targetName
    }

    override suspend fun saveLastCurrencyPair(sourceName: String, targetName: String) {
        dataStore.edit {
            it[SOURCE_CURRENCY_KEY] = sourceName
            it[TARGET_CURRENCY_KEY] = targetName
        }
    }

    override suspend fun getLastTrackedAmountCurrency(): String? =
        dataStore.data.first()[TRACKED_AMOUNT_CURRENCY_KEY]

    override suspend fun saveLastTrackedAmountCurrency(isoName: String) {
        dataStore.edit {
            it[TRACKED_AMOUNT_CURRENCY_KEY] = isoName
        }
    }
}