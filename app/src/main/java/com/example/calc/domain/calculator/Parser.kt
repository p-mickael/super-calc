package com.example.calc.domain.calculator

import com.example.calc.domain.calculator.model.Expression
import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.calculator.model.Token

sealed interface ParseResult {
    data class Success(val expression: Expression) : ParseResult
    data object Incomplete : ParseResult
}

class Parser(private val tokens: List<Token>) {
    companion object {
        private val ADDITIVE_OPERATORS = setOf(Operator.PLUS, Operator.MINUS)
        private val MULTIPLICATIVE_OPERATORS = setOf(Operator.TIMES, Operator.DIVIDE)
    }

    private var position = 0
    private val current get() = tokens.getOrNull(position)
    private fun advanceCursor() {
        position++
    }

    fun parse(): ParseResult {
        val result = parseExpression() ?: return ParseResult.Incomplete

        return if (position == tokens.size)
            ParseResult.Success(result)
        else
            ParseResult.Incomplete
    }

    private fun parseExpression(): Expression? = parseBinary(ADDITIVE_OPERATORS, ::parseTerm)
    private fun parseTerm(): Expression? = parseBinary(MULTIPLICATIVE_OPERATORS, ::parseUnary)

    private fun parseBinary(operators: Set<Operator>, next: () -> Expression?): Expression? {
        var left = next() ?: return null

        while (true) {
            val op = (current as? Token.Operator)?.operator
            if (op == null || op !in operators) break
            advanceCursor()
            val right = next() ?: return null
            left = Expression.BinaryOperator(op, left, right)
        }

        return left
    }

    private fun parseUnary(): Expression? {
        return if (current is Token.Negative) {
            advanceCursor()
            parsePostfix()?.let { Expression.Negate(it) }
        } else parsePostfix()
    }

    private fun parsePostfix(): Expression? {
        var operand = parsePrimary() ?: return null
        while (current is Token.Percent) {
            advanceCursor()
            operand = Expression.Percent(operand)
        }
        return operand
    }

    private fun parsePrimary(): Expression? = when (val token = current) {
        is Token.Number -> {
            advanceCursor()
            Expression.Number(token.text.toBigDecimal())
        }

        is Token.LeftParenthesis -> {
            advanceCursor()
            val inner = parseExpression() ?: return null
            if (current is Token.RightParenthesis) {
                advanceCursor()
                inner
            } else null
        }

        else -> null
    }
}