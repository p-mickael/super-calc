package com.example.calc.domain.conversion

interface RatesRepository {
    suspend fun getRates(): CurrencyRates?
}