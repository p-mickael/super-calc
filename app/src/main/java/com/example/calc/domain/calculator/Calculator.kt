package com.example.calc.domain.calculator

import java.math.BigDecimal

sealed interface CalculatorResult {
    data class Value(val value: BigDecimal) : CalculatorResult
    data object Incomplete : CalculatorResult
    data object DivisionByZero : CalculatorResult
}

open class Calculator {
    fun calculate(input: CalculatorInput): CalculatorResult =
        when (val parsed = Parser(input.tokens).parse()) {
            is ParseResult.Incomplete -> CalculatorResult.Incomplete
            is ParseResult.Success -> when (val result = Evaluator().evaluate(parsed.expression)) {
                is EvaluationResult.Success -> CalculatorResult.Value(result.value)
                EvaluationResult.DivisionByZero -> CalculatorResult.DivisionByZero
            }
        }

    open fun preview(input: CalculatorInput): BigDecimal? =
        (calculate(input) as? CalculatorResult.Value)?.value
}