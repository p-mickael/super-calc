package com.example.calc.domain.calculator.model

import java.math.BigDecimal

sealed interface Expression {
    data class Number(val value: BigDecimal) : Expression
    data class Negate(val operand: Expression) : Expression
    data class Percent(val operand: Expression) : Expression
    data class BinaryOperator(val operator: Operator, val left: Expression, val right: Expression) :
        Expression
}