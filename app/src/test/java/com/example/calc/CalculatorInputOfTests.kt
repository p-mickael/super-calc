package com.example.calc

import com.example.calc.domain.calculator.CalculatorInput
import com.example.calc.domain.calculator.model.Token
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CalculatorInputOfTests(
    @Suppress("unused") private val label: String,
    private val value: String,
    private val expected: List<Token>,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = listOf(
            arrayOf("zéro", "0", listOf(Token.Number("0"))),
            arrayOf("entier positif", "5", listOf(Token.Number("5"))),
            arrayOf("entier négatif", "-5", listOf(Token.Negative, Token.Number("5"))),
            arrayOf("zéros de fin supprimés", "1.50", listOf(Token.Number("1.5"))),
            arrayOf("décimal négatif", "-3.14", listOf(Token.Negative, Token.Number("3.14"))),
        )
    }

    @Test
    fun `of produces expected tokens`() {
        assertEquals(expected, CalculatorInput.of(value.toBigDecimal()).tokens)
    }
}
