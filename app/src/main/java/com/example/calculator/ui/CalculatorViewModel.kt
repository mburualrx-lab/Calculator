package com.example.calculator.ui

import androidx.lifecycle.ViewModel
import com.example.calculator.domain.CalculatorEngine
import com.example.calculator.domain.CalculatorState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CalculatorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CalculatorState())
    val uiState: StateFlow<CalculatorState> = _uiState.asStateFlow()

    fun onDigitInput(digit: String) {
        _uiState.update { current ->
            if (current.isFinalResult || current.error != null) {
                val newExpr = if (digit == ".") "0." else digit
                val state = CalculatorState(expression = newExpr, isFinalResult = false)
                updatePreview(state)
            } else {
                val expr = current.expression
                val currentOperand = getCurrentOperand(expr)

                if (digit == "." && currentOperand.contains(".")) {
                    current
                } else {
                    val newExpr = if (expr.isEmpty() && digit == ".") "0." else expr + digit
                    val state = current.copy(expression = newExpr, error = null)
                    updatePreview(state)
                }
            }
        }
    }

    fun onOperatorInput(op: String) {
        _uiState.update { current ->
            val expr = current.expression

            if (expr.isEmpty()) {
                if (op == "-") {
                    CalculatorState(expression = "-", isFinalResult = false)
                } else {
                    current
                }
            } else if (CalculatorEngine.isOperator(expr.last())) {
                val newExpr = expr.dropLast(1) + op
                val state = current.copy(expression = newExpr, isFinalResult = false, error = null)
                updatePreview(state)
            } else {
                val newExpr = "$expr $op "
                val state = current.copy(expression = newExpr, isFinalResult = false, error = null)
                updatePreview(state)
            }
        }
    }

    fun onEquals() {
        _uiState.update { current ->
            val expr = current.expression
            if (expr.isBlank()) return@update current

            when (val result = CalculatorEngine.evaluate(expr)) {
                is CalculatorEngine.Result.Success -> {
                    if (result.value.isEmpty()) {
                        current
                    } else {
                        CalculatorState(
                            expression = result.value,
                            resultPreview = "",
                            error = null,
                            isFinalResult = true
                        )
                    }
                }
                is CalculatorEngine.Result.Error -> {
                    current.copy(
                        error = result.message,
                        isFinalResult = true
                    )
                }
            }
        }
    }

    fun onClear() {
        _uiState.value = CalculatorState()
    }

    fun onBackspace() {
        _uiState.update { current ->
            if (current.isFinalResult || current.error != null) {
                CalculatorState()
            } else {
                val expr = current.expression
                if (expr.isEmpty()) return@update current

                val newExpr = if (expr.endsWith(" ")) {
                    expr.trimEnd().dropLast(1).trimEnd()
                } else {
                    expr.dropLast(1)
                }
                val state = current.copy(expression = newExpr, error = null)
                updatePreview(state)
            }
        }
    }

    fun onPercentage() {
        _uiState.update { current ->
            val expr = current.expression
            if (expr.isNotEmpty() && (expr.last().isDigit() || expr.last() == '%')) {
                val newExpr = "$expr%"
                val state = current.copy(expression = newExpr, error = null)
                updatePreview(state)
            } else {
                current
            }
        }
    }

    fun onToggleSign() {
        _uiState.update { current ->
            val expr = current.expression
            if (expr.isEmpty()) return@update current

            if (current.isFinalResult) {
                val toggled = if (expr.startsWith("-")) expr.substring(1) else "-$expr"
                CalculatorState(expression = toggled, isFinalResult = false)
            } else {
                val spaceIndex = expr.lastIndexOf(' ')
                if (spaceIndex != -1) {
                    val prefix = expr.substring(0, spaceIndex + 1)
                    val lastToken = expr.substring(spaceIndex + 1)
                    if (lastToken.isNotEmpty() && !CalculatorEngine.isOperator(lastToken.first())) {
                        val toggledToken = if (lastToken.startsWith("-")) {
                            lastToken.substring(1)
                        } else {
                            "-$lastToken"
                        }
                        val newExpr = prefix + toggledToken
                        val state = current.copy(expression = newExpr, error = null)
                        updatePreview(state)
                    } else {
                        current
                    }
                } else {
                    val toggled = if (expr.startsWith("-")) expr.substring(1) else "-$expr"
                    val state = current.copy(expression = toggled, error = null)
                    updatePreview(state)
                }
            }
        }
    }

    private fun getCurrentOperand(expr: String): String {
        val lastSpace = expr.lastIndexOf(' ')
        return if (lastSpace != -1) expr.substring(lastSpace + 1) else expr
    }

    private fun updatePreview(state: CalculatorState): CalculatorState {
        val expr = state.expression
        if (expr.isBlank() || !hasOperatorAndSecondOperand(expr)) {
            return state.copy(resultPreview = "")
        }

        return when (val result = CalculatorEngine.evaluate(expr)) {
            is CalculatorEngine.Result.Success -> {
                state.copy(resultPreview = result.value)
            }
            is CalculatorEngine.Result.Error -> {
                state.copy(resultPreview = "")
            }
        }
    }

    private fun hasOperatorAndSecondOperand(expr: String): Boolean {
        var opIndex = -1
        for (i in expr.indices) {
            if (CalculatorEngine.isOperator(expr[i])) {
                opIndex = i
                break
            }
        }
        if (opIndex == -1 || opIndex >= expr.length - 1) return false
        for (i in opIndex + 1 until expr.length) {
            val c = expr[i]
            if (c != ' ' && c != '%') {
                return true
            }
        }
        return false
    }
}
