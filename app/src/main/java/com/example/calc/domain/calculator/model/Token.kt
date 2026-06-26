package com.example.calc.domain.calculator.model

sealed interface Token {
    data class Number(val text: String) : Token
    data class Operator(val operator: com.example.calc.domain.calculator.model.Operator) : Token
    data object Negative : Token
    data object LeftParenthesis : Token
    data object RightParenthesis : Token
    data object Percent : Token
}