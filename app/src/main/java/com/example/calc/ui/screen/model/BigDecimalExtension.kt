package com.example.calc.ui.screen.model

import java.math.BigDecimal

fun BigDecimal?.toFormattedString(): String = this
    ?.let { it.stripTrailingZeros()?.toPlainString() }
    .orEmpty()