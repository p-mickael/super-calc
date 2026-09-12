package com.example.calc.domain

interface AppPreferenceStore {
    suspend fun getLastCalculatorMode(): CalculatorMode?
    suspend fun saveLastCalculatorMode(calculatorMode: CalculatorMode)
    suspend fun getLastCurrencyPair(): Pair<String, String>?
    suspend fun saveLastCurrencyPair(sourceName: String, targetName: String)
    suspend fun getLastTrackedAmountCurrency(): String?
    suspend fun saveLastTrackedAmountCurrency(isoName: String)
}