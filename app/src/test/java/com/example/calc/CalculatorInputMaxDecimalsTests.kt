package com.example.calc

import com.example.calc.domain.calculator.CalculatorInput
import com.example.calc.domain.calculator.model.Token
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorInputMaxDecimalsTests {
    @Test
    fun `appendDigit ignores digits beyond maxDecimals`() {
        val result = "1.239".fold(CalculatorInput.EMPTY) { input, key ->
            if (key.isDigit()) input.appendDigit(key, maxDecimals = 2) else input.appendDot()
        }

        assertEquals(listOf(Token.Number("1.23")), result.tokens)
    }

    @Test
    fun `appendDigit without maxDecimals keeps every digit typed`() {
        val result = "1.239".fold(CalculatorInput.EMPTY) { input, key ->
            if (key.isDigit()) input.appendDigit(key) else input.appendDot()
        }

        assertEquals(listOf(Token.Number("1.239")), result.tokens)
    }
}
