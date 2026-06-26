package com.example.calc.domain.conversion

class UnknownCurrencyException(isoName: String) : Exception("Unknown ISO name : $isoName")