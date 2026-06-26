package com.example.calc

import com.example.calc.domain.calculator.EvaluationResult
import com.example.calc.domain.calculator.Evaluator
import com.example.calc.domain.calculator.model.Expression
import com.example.calc.domain.calculator.model.Operator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.math.BigDecimal

// Helpers pour construire les expressions
private fun num(v: String) = Expression.Number(BigDecimal(v))
private fun neg(e: Expression) = Expression.Negate(e)
private fun pct(e: Expression) = Expression.Percent(e)
private fun plus(l: Expression, r: Expression) = Expression.BinaryOperator(Operator.PLUS, l, r)
private fun minus(l: Expression, r: Expression) = Expression.BinaryOperator(Operator.MINUS, l, r)
private fun times(l: Expression, r: Expression) = Expression.BinaryOperator(Operator.TIMES, l, r)
private fun div(l: Expression, r: Expression) = Expression.BinaryOperator(Operator.DIVIDE, l, r)

// BigDecimal.equals() est sensible à l'échelle (8 ≠ 8.0) — on passe par compareTo
private fun assertValue(expected: String, result: EvaluationResult) {
    assertTrue("Expected Success but got $result", result is EvaluationResult.Success)
    val actual = (result as EvaluationResult.Success).value
    assertEquals("Expected $expected but got $actual", 0, BigDecimal(expected).compareTo(actual))
}

@RunWith(Parameterized::class)
class EvaluatorSuccessTests(
    @Suppress("unused") private val label: String,
    private val expression: Expression,
    private val expected: String,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = listOf(

            // --- Feuilles ---
            arrayOf("integer number", num("5"), "5"),
            arrayOf("decimal number", num("3.14"), "3.14"),

            // --- Negate ---
            arrayOf("negate number", neg(num("5")), "-5"),
            arrayOf("negate expression", neg(plus(num("5"), num("3"))), "-8"),
            arrayOf("negate percent", neg(pct(num("50"))), "-0.5"),

            // --- Quatre opérateurs de base ---
            arrayOf("addition", plus(num("5"), num("3")), "8"),
            arrayOf("subtraction", minus(num("5"), num("3")), "2"),
            arrayOf("multiplication", times(num("5"), num("3")), "15"),
            arrayOf("division", div(num("8"), num("4")), "2"),

            // --- Sémantique du % (spec Android) ---

            // b% seul = b/100
            arrayOf("standalone percent", pct(num("15")), "0.15"),

            // a + b% = a * (1 + b/100)   →  100 + 15% = 115
            arrayOf("contextual percent with +", plus(num("100"), pct(num("15"))), "115"),

            // a - b% = a * (1 - b/100)   →  100 - 15% = 85
            arrayOf("contextual percent with -", minus(num("100"), pct(num("15"))), "85"),

            // a × b% = a * (b/100)        →  100 × 15% = 15
            arrayOf("plain percent with *", times(num("100"), pct(num("15"))), "15"),

            // a ÷ b% = a / (b/100)        →  100 ÷ 20% = 500
            arrayOf("plain percent with /", div(num("100"), pct(num("20"))), "500"),

            // Le % contextuel ne s'applique que si le nœud droit est DIRECTEMENT un Percent.
            // 100 + -15% = 100 + (-0.15) = 99.85  (≠  100 - 15% = 85)
            arrayOf(
                "negate(percent) as right of + is NOT contextual",
                plus(num("100"), neg(pct(num("15")))),
                "99.85",
            ),

            // --- Expressions composées ---
            arrayOf(
                "compound expression",
                plus(num("10"), times(num("3"), num("4"))),
                "22",
            ),
            arrayOf(
                "percent as right in compound",
                plus(num("200"), pct(plus(num("5"), num("5")))),  // 200 + (5+5)% = 200 + 10% = 220
                "220",
            ),
            arrayOf(
                "irrational division",
                div(num("1"), num("3")),
                "0.3333333333333333"
            ),
            arrayOf(
                "chained percent",
                plus(plus(num("100"), pct(num("15"))), pct(num("10"))),
                "126.5"
            )
        )
    }

    @Test
    fun `expression evaluates to expected value`() {
        assertValue(expected, Evaluator().evaluate(expression))
    }
}

@RunWith(Parameterized::class)
class EvaluatorDivisionByZeroTests(
    @Suppress("unused") private val label: String,
    private val expression: Expression,
) {
    companion object {
        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun cases(): List<Array<Any>> = listOf(
            arrayOf("direct division by zero", div(num("5"), num("0"))),
            arrayOf("division by zero percent", div(num("5"), pct(num("0")))),  // 0% = 0
        )
    }

    @Test
    fun `expression evaluates to DivisionByZero`() {
        assertEquals(EvaluationResult.DivisionByZero, Evaluator().evaluate(expression))
    }
}