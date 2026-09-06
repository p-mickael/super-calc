package com.example.calc.domain.calculator

import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.calculator.model.Token
import java.math.BigDecimal

class CalculatorInput private constructor(val tokens: List<Token>) {
    init {
        require(isPrefixValid(tokens)) { "Séquence invalid : $tokens" }
    }

    private enum class Link { DIRECT, NEEDS_TIMES, FORBIDDEN }
    companion object {
        val EMPTY = CalculatorInput(emptyList())

        private fun isPrefixValid(tokens: List<Token>): Boolean {
            var parenthesisBalance = 0
            var previous: Token? = null
            for (token in tokens) {
                if (getLink(previous, token) != Link.DIRECT) return false
                if (token is Token.LeftParenthesis) parenthesisBalance++
                if (token is Token.RightParenthesis) parenthesisBalance--
                if (parenthesisBalance < 0) return false
                previous = token
            }

            return true
        }

        private fun isLeftTokenValue(token: Token?): Boolean = when (token) {
            is Token.Number, Token.RightParenthesis, Token.Percent -> true
            Token.LeftParenthesis, is Token.Operator, Token.Negative, null -> false
        }

        private fun isRightTokenValue(token: Token): Boolean = when (token) {
            is Token.Number, Token.LeftParenthesis, Token.Negative -> true
            Token.RightParenthesis, is Token.Operator, Token.Percent -> false
        }

        private fun getLink(left: Token?, right: Token): Link {
            val leftIsValue = isLeftTokenValue(left)
            val rightIsValue = isRightTokenValue(right)

            return when {
                leftIsValue && rightIsValue -> Link.NEEDS_TIMES
                leftIsValue != rightIsValue -> Link.DIRECT
                else -> Link.FORBIDDEN
            }
        }

        fun of(value: BigDecimal): CalculatorInput {
            val valueText = value.abs().stripTrailingZeros().toPlainString()
            return CalculatorInput(
                if (value.signum() < 0) listOf(Token.Negative, Token.Number(valueText))
                else listOf(Token.Number(valueText))
            )
        }

        fun fromTokens(tokens: List<Token>): CalculatorInput? =
            runCatching { CalculatorInput(tokens) }.getOrNull()
    }

    private val last get() = tokens.lastOrNull()
    private val lastTokenIsValue = isLeftTokenValue(last)
    private val lastTokenIsNotValue = !lastTokenIsValue

    fun appendDigit(char: Char): CalculatorInput {
        if (!char.isDigit()) return this

        return when (val current = last) {
            is Token.Number ->
                replaceLast(
                    current.copy(
                        text =
                            when {
                                current.text == "0" -> char.toString()
                                else -> current.text + char
                            }
                    )
                )

            else -> append(Token.Number(char.toString()))
        }
    }

    fun appendPercent(): CalculatorInput = when (getLink(last, Token.Percent)) {
        Link.DIRECT -> append(Token.Percent)
        else -> replaceLastNonValues(Token.Percent)
    }

    fun appendOperator(operator: Operator): CalculatorInput = when {
        operator == Operator.MINUS && lastTokenIsNotValue && last !is Token.Negative ->
            CalculatorInput(tokens + Token.Negative)

        getLink(last, Token.Operator(operator)) == Link.DIRECT -> append(Token.Operator(operator))
        else -> replaceLastNonValues(Token.Operator(operator))
    }

    fun appendDot(): CalculatorInput = when (val number = last) {
        is Token.Number -> when {
            number.text.contains('.') -> this
            else -> replaceLast(number.copy(text = number.text + '.'))
        }

        else -> append(Token.Number("0."))
    }

    fun openParenthesis(): CalculatorInput = append(Token.LeftParenthesis)

    fun closeParenthesis(): CalculatorInput {
        val noOpenedParen =
            tokens.count { it is Token.LeftParenthesis } - tokens.count { it is Token.RightParenthesis } < 1

        return when {
            getLink(last, Token.RightParenthesis) == Link.FORBIDDEN || noOpenedParen -> this
            else -> append(Token.RightParenthesis)
        }
    }

    fun deleteLast(): CalculatorInput {
        if (last == null)
            return this

        val lastNumber = last as? Token.Number

        return when {
            lastNumber != null && lastNumber.text.length > 1 ->
                replaceLast(lastNumber.copy(text = lastNumber.text.dropLast(1)))

            else ->
                CalculatorInput(tokens.dropLast(1))
        }
    }

    fun clear(): CalculatorInput = EMPTY

    private fun replaceLast(token: Token) = CalculatorInput(tokens.dropLast(1) + token)
    private fun replaceLastNonValues(token: Token): CalculatorInput {
        val newTokens = tokens
            .take(tokens.indexOfLast { isLeftTokenValue(it) } + 1) + token

        return when {
            isPrefixValid(newTokens) -> CalculatorInput(newTokens)
            else -> this
        }
    }

    private fun append(newToken: Token): CalculatorInput = when (getLink(last, newToken)) {
        Link.NEEDS_TIMES -> CalculatorInput(tokens + Token.Operator(Operator.TIMES) + newToken)
        else -> CalculatorInput(tokens + newToken)
    }
}