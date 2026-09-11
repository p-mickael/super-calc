package com.example.calc.domain.conversion

import java.math.BigDecimal
import java.math.MathContext

class CurrencyConverter(
    private val rates: CurrencyRates,
    private val sourceIsoName: String,
    private val targetIsoName: String,
) {
    init {
        require(rates.rateExists(sourceIsoName)) { "Unknown currency ISO : $sourceIsoName" }
        require(rates.rateExists(targetIsoName)) { "Unknown currency ISO : $targetIsoName" }
    }

    fun convert(amount: BigDecimal): BigDecimal {
        val sourceRate = rates.getRate(sourceIsoName)
        val targetRate = rates.getRate(targetIsoName)

        return amount
            .divide(sourceRate, MathContext.DECIMAL64)
            .multiply(targetRate, MathContext.DECIMAL64)
    }
}