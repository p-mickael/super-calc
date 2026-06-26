package com.example.calc.domain

interface AppPreferenceStore {
    suspend fun getLastCalculatorMode(): CalculatorMode?
    suspend fun saveLastCalculatorMode(calculatorMode: CalculatorMode)
}