package com.example.calc

import com.example.calc.domain.calculator.CalculatorInput
import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.calculator.model.Token
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CalculatorInputTests(
    @Suppress("unused") private val label: String,
    private val keys: String,
    private val expected: List<Token>
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = listOf(
            arrayOf("second point ignored", "1..", listOf(Token.Number("1."))),
            arrayOf("leading zero dropped", "05", listOf(Token.Number("5"))),
            arrayOf("leading dot become 0.", ".", listOf(Token.Number("0."))),
            arrayOf(
                "binary minus", "5-4", listOf(
                    Token.Number("5"), Token.Operator(Operator.MINUS),
                    Token.Number("4")
                )
            ),
            arrayOf(
                "unary minus after operator",
                "5*-4",
                listOf(
                    Token.Number("5"),
                    Token.Operator(Operator.TIMES),
                    Token.Negative,
                    Token.Number("4")
                )
            ),
            arrayOf("leading unary minus", "-5", listOf(Token.Negative, Token.Number("5"))),
            arrayOf(
                "fixed operator",
                "5*+4",
                listOf(Token.Number("5"), Token.Operator(Operator.PLUS), Token.Number("4"))
            ),
            arrayOf(
                "implicit multiplication between right and left parenthesis", "(5)(", listOf(
                    Token.LeftParenthesis,
                    Token.Number("5"),
                    Token.RightParenthesis,
                    Token.Operator(Operator.TIMES),
                    Token.LeftParenthesis
                )
            ),
            arrayOf(
                "implicit multiplication between number and left parenthesis", "5(", listOf(
                    Token.Number("5"), Token.Operator(
                        Operator.TIMES
                    ), Token.LeftParenthesis
                )
            ),
            arrayOf(
                "right parenthesis ignored if not opened",
                "4)",
                listOf(Token.Number("4"))
            ),
            arrayOf(
                "parenthesis opened and closed", "(5+4)", listOf(
                    Token.LeftParenthesis,
                    Token.Number("5"),
                    Token.Operator(Operator.PLUS),
                    Token.Number("4"),
                    Token.RightParenthesis
                )
            ),
            arrayOf(
                "percent added to integer", "5%", listOf(
                    Token.Number("5"), Token.Percent
                )
            ),
            arrayOf(
                "percent added to decimal", "5.4%", listOf(
                    Token.Number("5.4"), Token.Percent
                )
            ),
            arrayOf(
                "percent added to parenthesis value with decimal", "(5,2+4,7)%", listOf(
                    Token.LeftParenthesis,
                    Token.Number("5.2"),
                    Token.Operator(Operator.PLUS),
                    Token.Number("4.7"),
                    Token.RightParenthesis,
                    Token.Percent
                )
            ),
            arrayOf(
                "percent replace operator", "5+%", listOf(
                    Token.Number("5"), Token.Percent
                )
            ),
            arrayOf(
                "percent replace left parenthesis", "5(%", listOf(
                    Token.Number("5"), Token.Percent
                )
            ),
            arrayOf(
                "operator replace left parenthesis", "5(+", listOf(
                    Token.Number("5"), Token.Operator(Operator.PLUS)
                )
            ),
            arrayOf(
                "unary minus doesn't replace left parenthesis", "5(-", listOf(
                    Token.Number("5"),
                    Token.Operator(Operator.TIMES),
                    Token.LeftParenthesis,
                    Token.Negative
                )
            ),
            arrayOf(
                "operator deleting all expression is ignored", "(+", listOf(
                    Token.LeftParenthesis
                )
            ),
        )
    }

    @Test
    fun `correct in input produce correct tokens`() {
        val result = CalculatorInput.EMPTY.applyKeys(keys)
        assertEquals(expected, result.tokens)
    }

    private fun CalculatorInput.applyKeys(keys: String): CalculatorInput =
        keys.fold(this) { input, key ->
            when (key) {
                in '0'..'9' -> input.appendDigit(key)
                '.' -> input.appendDot()
                ',' -> input.appendDot()
                '+' -> input.appendOperator(Operator.PLUS)
                '-' -> input.appendOperator(Operator.MINUS)
                '*' -> input.appendOperator(Operator.TIMES)
                '/' -> input.appendOperator(Operator.DIVIDE)
                '(' -> input.openParenthesis()
                ')' -> input.closeParenthesis()
                '%' -> input.appendPercent()
                else -> error("Unknown key : $key")
            }
        }
}