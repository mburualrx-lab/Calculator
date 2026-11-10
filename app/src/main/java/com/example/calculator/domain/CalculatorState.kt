package com.example.calculator.domain

data class CalculatorState(
    val expression: String = "",
    val resultPreview: String = "",
    val error: String? = null,
    val isFinalResult: Boolean = false
)
