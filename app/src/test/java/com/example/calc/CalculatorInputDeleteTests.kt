package com.example.calc

import com.example.calc.domain.calculator.CalculatorInput
import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.calculator.model.Token
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculatorInputDeleteTests {

    @Test
    fun `delete on empty stays empty`() {
        assertEquals(emptyList<Token>(), CalculatorInput.EMPTY.deleteLast().tokens)
    }

    @Test
    fun `delete reduces a multi-digit number`() {
        val input = CalculatorInput.EMPTY.appendDigit('1').appendDigit('2').appendDigit('3')
        assertEquals(listOf(Token.Number("12")), input.deleteLast().tokens)
    }

    @Test
    fun `delete removes a single-digit number entirely`() {
        val input = CalculatorInput.EMPTY.appendDigit('5')
        assertEquals(emptyList<Token>(), input.deleteLast().tokens)
    }

    @Test
    fun `delete a parenthesis leaves the implicit times`() {
        // 5( produit [5, ×, (] ; supprimer ( laisse le × implicite
        val input = CalculatorInput.EMPTY.appendDigit('5').openParenthesis()
        assertEquals(
            listOf(Token.Number("5"), Token.Operator(Operator.TIMES)),
            input.deleteLast().tokens
        )
    }

    @Test
    fun `delete a trailing dot keeps the integer part`() {
        val input = CalculatorInput.EMPTY.appendDigit('0').appendDot() // "0."
        assertEquals(listOf(Token.Number("0")), input.deleteLast().tokens)
    }
}
