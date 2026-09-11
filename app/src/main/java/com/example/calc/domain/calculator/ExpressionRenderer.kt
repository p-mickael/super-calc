package com.example.calc.domain.calculator

import com.example.calc.domain.calculator.model.Operator
import com.example.calc.domain.calculator.model.Token

fun List<Token>.renderExpression(): String = joinToString("") { token ->
    when (token) {
        is Token.Number -> token.text
        is Token.Operator -> when (token.operator) {
            Operator.PLUS -> "+"
            Operator.MINUS -> "–"
            Operator.TIMES -> "×"
            Operator.DIVIDE -> "÷"
        }

        is Token.Percent -> "%"
        is Token.Negative -> "-"
        is Token.LeftParenthesis -> "("
        is Token.RightParenthesis -> ")"
    }
}
