package com.example.calc.domain.calculator

import com.example.calc.domain.calculator.model.Expression
import com.example.calc.domain.calculator.model.Operator
import java.math.BigDecimal
import java.math.MathContext

private val HUNDRED = BigDecimal(100)
private val MATH_CONTEXT = MathContext.DECIMAL64

sealed interface EvaluationResult {
    data class Success(val value: BigDecimal) : EvaluationResult
    data object DivisionByZero : EvaluationResult
}

class Evaluator {
    fun evaluate(expression: Expression): EvaluationResult =
        try {
            EvaluationResult.Success(evaluateExpression(expression))
        } catch (e: ArithmeticException) {
            EvaluationResult.DivisionByZero
        }

    private fun evaluateExpression(expression: Expression): BigDecimal = when (expression) {
        is Expression.Number -> expression.value
        is Expression.Negate -> evaluateExpression(expression.operand).negate()
        is Expression.Percent -> evaluateExpression(expression.operand).divide(
            HUNDRED,
            MATH_CONTEXT
        )

        is Expression.BinaryOperator -> evaluateBinary(expression)
    }

    private val percentModifierOperators = setOf(Operator.PLUS, Operator.MINUS)
    private fun evaluateBinary(node: Expression.BinaryOperator): BigDecimal {
        val leftValue = evaluateExpression(node.left)
        val right = node.right

        if (right is Expression.Percent)
            if (node.operator == Operator.PLUS)
                return leftValue.increaseBy(rate = right)
            else if (node.operator == Operator.MINUS)
                return leftValue.decreaseBy(rate = right)

        val rightValue = evaluateExpression(right)
        return when (node.operator) {
            Operator.PLUS -> leftValue.add(rightValue, MATH_CONTEXT)
            Operator.MINUS -> leftValue.subtract(rightValue, MATH_CONTEXT)
            Operator.TIMES -> leftValue.multiply(rightValue, MATH_CONTEXT)
            Operator.DIVIDE -> {
                if (rightValue.signum() == 0) throw ArithmeticException("Division by zero")
                leftValue.divide(rightValue, MATH_CONTEXT)
            }
        }
    }

    private fun BigDecimal.increaseBy(rate: Expression.Percent): BigDecimal =
        this.multiply(BigDecimal.ONE + evaluateExpression(rate), MATH_CONTEXT)

    private fun BigDecimal.decreaseBy(rate: Expression.Percent): BigDecimal =
        this.multiply(BigDecimal.ONE - evaluateExpression(rate), MATH_CONTEXT)
}