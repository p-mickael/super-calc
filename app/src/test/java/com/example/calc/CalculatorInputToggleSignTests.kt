package com.example.calc

import com.example.calc.domain.calculator.CalculatorInput
import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.calculator.model.Token
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorInputToggleSignTests {
    @Test
    fun `toggling sign of a positive number makes it negative`() {
        val result = CalculatorInput.EMPTY.appendDigit('5').toggleSign()

        assertEquals(listOf(Token.Negative, Token.Number("5")), result.tokens)
    }

    @Test
    fun `toggling sign twice returns to the original positive number`() {
        val result = CalculatorInput.EMPTY.appendDigit('5').toggleSign().toggleSign()

        assertEquals(listOf(Token.Number("5")), result.tokens)
    }

    @Test
    fun `toggling sign on empty input prepares a negative number`() {
        val result = CalculatorInput.EMPTY.toggleSign()

        assertEquals(listOf(Token.Negative), result.tokens)
    }

    @Test
    fun `toggling sign twice on empty input cancels back to empty`() {
        val result = CalculatorInput.EMPTY.toggleSign().toggleSign()

        assertEquals(emptyList<Token>(), result.tokens)
    }

    @Test
    fun `toggling sign right after an operator prepares a negative number`() {
        val result = CalculatorInput.EMPTY
            .appendDigit('5')
            .appendOperator(Operator.PLUS)
            .toggleSign()

        assertEquals(
            listOf(Token.Number("5"), Token.Operator(Operator.PLUS), Token.Negative),
            result.tokens
        )
    }

    @Test
    fun `toggling sign after a percent is a no-op`() {
        val input = CalculatorInput.EMPTY.appendDigit('5').appendPercent()

        val result = input.toggleSign()

        assertEquals(input.tokens, result.tokens)
    }

    @Test
    fun `toggling sign after a closed parenthesis is a no-op`() {
        val input = CalculatorInput.EMPTY
            .openParenthesis()
            .appendDigit('5')
            .closeParenthesis()

        val result = input.toggleSign()

        assertEquals(input.tokens, result.tokens)
    }
}
