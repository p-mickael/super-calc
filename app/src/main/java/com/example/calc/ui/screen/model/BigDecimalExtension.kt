package com.example.calc.ui.screen.model

import java.math.BigDecimal
import java.math.RoundingMode

fun BigDecimal?.toFormattedString(maxDecimals: Int? = null): String = this
    ?.let { value ->
        maxDecimals?.let { value.setScale(it, RoundingMode.HALF_UP).toPlainString() }
            ?: value.stripTrailingZeros().toPlainString()
    }
    .orEmpty()