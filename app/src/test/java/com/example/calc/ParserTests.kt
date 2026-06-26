package com.example.calc

import com.example.calc.domain.calculator.ParseResult
import com.example.calc.domain.calculator.Parser
import com.example.calc.domain.calculator.model.Expression
import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.calculator.model.Token
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

// Helpers pour rendre les cas lisibles
private fun num(v: String) = Expression.Number(v.toBigDecimal())
private fun neg(e: Expression) = Expression.Negate(e)
private fun pct(e: Expression) = Expression.Percent(e)
private fun plus(l: Expression, r: Expression) = Expression.BinaryOperator(Operator.PLUS, l, r)
private fun minus(l: Expression, r: Expression) = Expression.BinaryOperator(Operator.MINUS, l, r)
private fun times(l: Expression, r: Expression) = Expression.BinaryOperator(Operator.TIMES, l, r)
private fun div(l: Expression, r: Expression) = Expression.BinaryOperator(Operator.DIVIDE, l, r)

@RunWith(Parameterized::class)
class ParserSuccessTests(
    @Suppress("unused") private val label: String,
    private val tokens: List<Token>,
    private val expected: Expression,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = listOf(

            // --- Atomiques ---
            arrayOf("single integer", listOf(Token.Number("5")), num("5")),
            arrayOf("decimal number", listOf(Token.Number("3.14")), num("3.14")),
            arrayOf("trailing dot", listOf(Token.Number("3.")), num("3")),
            arrayOf(
                "negative negative",
                listOf(
                    Token.Negative,
                    Token.LeftParenthesis,
                    Token.Negative,
                    Token.Number("3"),
                    Token.RightParenthesis
                ),
                neg(neg(num("3")))
            ),

            // --- Quatre opérateurs ---
            arrayOf(
                "addition",
                listOf(Token.Number("5"), Token.Operator(Operator.PLUS), Token.Number("3")),
                plus(num("5"), num("3")),
            ),
            arrayOf(
                "subtraction",
                listOf(Token.Number("5"), Token.Operator(Operator.MINUS), Token.Number("3")),
                minus(num("5"), num("3")),
            ),
            arrayOf(
                "multiplication",
                listOf(Token.Number("5"), Token.Operator(Operator.TIMES), Token.Number("3")),
                times(num("5"), num("3")),
            ),
            arrayOf(
                "division",
                listOf(Token.Number("8"), Token.Operator(Operator.DIVIDE), Token.Number("4")),
                div(num("8"), num("4")),
            ),

            // --- Précédence ---
            arrayOf(
                "* binds tighter than + (right side)",
                listOf(
                    Token.Number("5"), Token.Operator(Operator.PLUS),
                    Token.Number("3"), Token.Operator(Operator.TIMES), Token.Number("2"),
                ),
                plus(num("5"), times(num("3"), num("2"))),
            ),
            arrayOf(
                "* binds tighter than + (left side)",
                listOf(
                    Token.Number("5"), Token.Operator(Operator.TIMES),
                    Token.Number("3"), Token.Operator(Operator.PLUS), Token.Number("2"),
                ),
                plus(times(num("5"), num("3")), num("2")),
            ),

            // --- Associativité gauche ---
            arrayOf(
                "subtraction is left-associative",
                listOf(
                    Token.Number("5"), Token.Operator(Operator.MINUS),
                    Token.Number("3"), Token.Operator(Operator.MINUS), Token.Number("2"),
                ),
                minus(minus(num("5"), num("3")), num("2")),  // (5-3)-2, pas 5-(3-2)
            ),
            arrayOf(
                "division is left-associative",
                listOf(
                    Token.Number("12"), Token.Operator(Operator.DIVIDE),
                    Token.Number("4"), Token.Operator(Operator.DIVIDE), Token.Number("3"),
                ),
                div(div(num("12"), num("4")), num("3")),
            ),

            // --- Parenthèses ---
            arrayOf(
                "parentheses override precedence",
                listOf(
                    Token.LeftParenthesis,
                    Token.Number("5"), Token.Operator(Operator.PLUS), Token.Number("3"),
                    Token.RightParenthesis,
                    Token.Operator(Operator.TIMES), Token.Number("2"),
                ),
                times(plus(num("5"), num("3")), num("2")),
            ),
            arrayOf(
                "redundant parentheses around number",
                listOf(Token.LeftParenthesis, Token.Number("5"), Token.RightParenthesis),
                num("5"),
            ),
            arrayOf(
                "nested parentheses",
                listOf(
                    Token.LeftParenthesis,
                    Token.LeftParenthesis, Token.Number("5"), Token.RightParenthesis,
                    Token.RightParenthesis,
                ),
                num("5"),
            ),

            // --- Moins unaire ---
            arrayOf(
                "unary minus on number",
                listOf(Token.Negative, Token.Number("5")),
                neg(num("5")),
            ),
            arrayOf(
                "unary minus on parenthesized expression",
                listOf(
                    Token.Negative,
                    Token.LeftParenthesis,
                    Token.Number("5"), Token.Operator(Operator.PLUS), Token.Number("3"),
                    Token.RightParenthesis,
                ),
                neg(plus(num("5"), num("3"))),
            ),
            arrayOf(
                "unary minus as right operand",
                listOf(
                    Token.Number("5"), Token.Operator(Operator.TIMES),
                    Token.Negative, Token.Number("3"),
                ),
                times(num("5"), neg(num("3"))),
            ),

            // --- Pourcent ---
            arrayOf(
                "percent on number",
                listOf(Token.Number("5"), Token.Percent),
                pct(num("5")),
            ),
            arrayOf(
                "percent on parenthesized expression",
                listOf(
                    Token.LeftParenthesis,
                    Token.Number("5"), Token.Operator(Operator.PLUS), Token.Number("3"),
                    Token.RightParenthesis,
                    Token.Percent,
                ),
                pct(plus(num("5"), num("3"))),
            ),
            arrayOf(
                "percent as right operand of addition",
                listOf(
                    Token.Number("100"), Token.Operator(Operator.PLUS),
                    Token.Number("15"), Token.Percent,
                ),
                plus(num("100"), pct(num("15"))),
            ),
            arrayOf(
                "unary minus then percent",
                listOf(Token.Negative, Token.Number("5"), Token.Percent),
                neg(pct(num("5"))),
            ),
            arrayOf(
                "Percent of a percent",
                listOf(Token.Number("5"), Token.Percent, Token.Percent),
                pct(pct(num("5")))
            ),
            arrayOf(
                "postfix priority over multiplication",
                listOf(
                    Token.Number("3"),
                    Token.Operator(Operator.TIMES),
                    Token.Number("5"),
                    Token.Percent
                ),
                times(num("3"), pct(num("5")))
            )
        )
    }

    @Test
    fun `tokens parse to expected expression`() {
        assertEquals(ParseResult.Success(expected), Parser(tokens).parse())
    }
}

@RunWith(Parameterized::class)
class ParserIncompleteTests(
    @Suppress("unused") private val label: String,
    private val tokens: List<Token>,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = listOf(
            arrayOf("empty", emptyList<Token>()),
            arrayOf("just negative", listOf(Token.Negative)),
            arrayOf("just open paren", listOf(Token.LeftParenthesis)),
            arrayOf(
                "trailing addition",
                listOf(Token.Number("5"), Token.Operator(Operator.PLUS)),
            ),
            arrayOf(
                "trailing multiplication",
                listOf(Token.Number("5"), Token.Operator(Operator.TIMES)),
            ),
            arrayOf(
                "trailing unary minus",
                listOf(Token.Number("5"), Token.Operator(Operator.TIMES), Token.Negative),
            ),
            arrayOf(
                "unclosed parenthesis",
                listOf(Token.LeftParenthesis, Token.Number("5")),
            ),
            arrayOf(
                "unclosed paren with trailing operator",
                listOf(
                    Token.LeftParenthesis,
                    Token.Number("5"), Token.Operator(Operator.PLUS),
                ),
            ),
        )
    }

    @Test
    fun `tokens parse as incomplete`() {
        assertEquals(ParseResult.Incomplete, Parser(tokens).parse())
    }
}