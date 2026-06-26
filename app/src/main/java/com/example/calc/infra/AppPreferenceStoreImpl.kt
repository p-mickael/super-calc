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
}