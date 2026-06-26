package com.example.calc

import com.example.calc.domain.conversion.CurrencyConverter
import com.example.calc.domain.conversion.CurrencyRates
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.math.BigDecimal
import kotlin.time.Instant

// Taux EUR-based (comme frankfurter) : 1 EUR = 184.88 JPY = 1.1467 USD
private val testRates = CurrencyRates.fromMap(
    mapOf(
        "EUR" to BigDecimal.ONE,
        "JPY" to "184.88".toBigDecimal(),
        "USD" to "1.1467".toBigDecimal(),
    ),
    Instant.parse("2026-06-19T16:00:00Z")
)

@RunWith(Parameterized::class)
class CurrencyConverterTests(
    @Suppress("unused") private val label: String,
    private val source: String,
    private val target: String,
    private val amount: String,
    private val expected: String,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = listOf(
            arrayOf("EUR→JPY", "EUR", "JPY", "1", "184.88"),
            arrayOf("JPY→EUR", "JPY", "EUR", "184.88", "1"),
            arrayOf("USD→JPY via pivot EUR", "USD", "JPY", "1.1467", "184.88"),
            arrayOf("identité EUR→EUR", "EUR", "EUR", "100", "100"),
        )
    }

    @Test
    fun `convert returns expected amount`() {
        val result = CurrencyConverter(testRates, source, target).convert(amount.toBigDecimal())
        assertEquals(0, expected.toBigDecimal().compareTo(result))
    }
}

class CurrencyConverterValidationTests {
    @Test
    fun `unknown source currency is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            CurrencyConverter(testRates, "XXX", "JPY")
        }
    }

    @Test
    fun `unknown target currency is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            CurrencyConverter(testRates, "EUR", "XXX")
        }
    }
}
