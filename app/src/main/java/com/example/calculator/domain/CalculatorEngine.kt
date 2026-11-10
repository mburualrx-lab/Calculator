package com.example.calculator.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object CalculatorEngine {

    const val OPERATORS = "+-×÷"

    private val decimalSymbols = DecimalFormatSymbols(Locale.US)
    private val integerFormatter = DecimalFormat("#,##0", decimalSymbols)
    private val scientificFormatter = DecimalFormat("0.######E0", decimalSymbols)

    fun isOperator(char: Char): Boolean = OPERATORS.contains(char)

    fun evaluate(expression: String): Result {
        if (expression.isBlank()) return Result.Success("")

        val sanitized = sanitizeExpression(expression)
        if (sanitized.isEmpty()) return Result.Success("")

        return try {
            val tokens = tokenize(sanitized)
            if (tokens.isEmpty()) return Result.Success("")
            val value = evaluateTokens(tokens)
            Result.Success(formatNumber(value))
        } catch (e: ArithmeticException) {
            Result.Error(e.message ?: "Calculation error")
        } catch (e: Exception) {
            Result.Error("Invalid expression")
        }
    }

    private fun sanitizeExpression(expr: String): String {
        var trimmed = expr.trim()
        while (trimmed.isNotEmpty() && isOperator(trimmed.last())) {
            trimmed = trimmed.substring(0, trimmed.length - 1).trim()
        }
        return trimmed
    }

    private sealed class Token {
        data class NumberToken(val value: BigDecimal) : Token()
        data class OperatorToken(val op: Char) : Token()
        data class PercentageToken(val value: BigDecimal) : Token()
    }

    private fun tokenize(expr: String): List<Token> {
        val tokens = mutableListOf<Token>()
        var index = 0
        val length = expr.length

        while (index < length) {
            val c = expr[index]

            if (c.isWhitespace()) {
                index++
                continue
            }

            if (isOperator(c)) {
                val isUnaryMinus = c == '-' && (tokens.isEmpty() || tokens.last() is Token.OperatorToken)
                if (isUnaryMinus) {
                    val numberBuilder = StringBuilder("-")
                    index++
                    while (index < length && (expr[index].isDigit() || expr[index] == '.')) {
                        numberBuilder.append(expr[index])
                        index++
                    }
                    if (numberBuilder.length > 1 && numberBuilder.toString() != "-.") {
                        val numStr = numberBuilder.toString()
                        if (index < length && expr[index] == '%') {
                            tokens.add(Token.PercentageToken(BigDecimal(numStr)))
                            index++
                        } else {
                            tokens.add(Token.NumberToken(BigDecimal(numStr)))
                        }
                    }
                    continue
                } else {
                    tokens.add(Token.OperatorToken(c))
                    index++
                    continue
                }
            }

            if (c.isDigit() || c == '.') {
                val numberBuilder = StringBuilder()
                while (index < length && (expr[index].isDigit() || expr[index] == '.')) {
                    numberBuilder.append(expr[index])
                    index++
                }
                val numStr = numberBuilder.toString()
                if (numStr.isNotEmpty() && numStr != ".") {
                    if (index < length && expr[index] == '%') {
                        tokens.add(Token.PercentageToken(BigDecimal(numStr)))
                        index++
                    } else {
                        tokens.add(Token.NumberToken(BigDecimal(numStr)))
                    }
                }
                continue
            }

            index++
        }
        return tokens
    }

    private fun evaluateTokens(tokens: List<Token>): BigDecimal {
        if (tokens.isEmpty()) return BigDecimal.ZERO

        val firstPassTokens = mutableListOf<Token>()
        var i = 0
        val size = tokens.size

        while (i < size) {
            val current = tokens[i]
            if (current is Token.PercentageToken) {
                val prevToken = if (firstPassTokens.isNotEmpty()) firstPassTokens.last() else null
                val prevOp = if (firstPassTokens.size >= 2) firstPassTokens[firstPassTokens.size - 2] else null

                val pctValue = if (prevToken is Token.NumberToken && prevOp is Token.OperatorToken) {
                    if (prevOp.op == '+' || prevOp.op == '-') {
                        prevToken.value.multiply(current.value).divide(BigDecimal("100"), MathContext.DECIMAL128)
                    } else {
                        current.value.divide(BigDecimal("100"), MathContext.DECIMAL128)
                    }
                } else {
                    current.value.divide(BigDecimal("100"), MathContext.DECIMAL128)
                }
                firstPassTokens.add(Token.NumberToken(pctValue))
                i++
            } else {
                firstPassTokens.add(current)
                i++
            }
        }

        val secondPassTokens = mutableListOf<Token>()
        var j = 0
        val firstPassSize = firstPassTokens.size

        while (j < firstPassSize) {
            val token = firstPassTokens[j]
            if (token is Token.OperatorToken && (token.op == '×' || token.op == '÷')) {
                val left = (secondPassTokens.removeAt(secondPassTokens.size - 1) as Token.NumberToken).value
                if (j + 1 < firstPassSize && firstPassTokens[j + 1] is Token.NumberToken) {
                    val right = (firstPassTokens[j + 1] as Token.NumberToken).value
                    val res = if (token.op == '×') {
                        left.multiply(right)
                    } else {
                        if (right.compareTo(BigDecimal.ZERO) == 0) {
                            throw ArithmeticException("Cannot divide by zero")
                        }
                        left.divide(right, 12, RoundingMode.HALF_UP)
                    }
                    secondPassTokens.add(Token.NumberToken(res))
                    j += 2
                } else {
                    secondPassTokens.add(token)
                    j++
                }
            } else {
                secondPassTokens.add(token)
                j++
            }
        }

        if (secondPassTokens.isEmpty()) return BigDecimal.ZERO
        var result = (secondPassTokens[0] as? Token.NumberToken)?.value ?: BigDecimal.ZERO

        var k = 1
        val secondPassSize = secondPassTokens.size
        while (k < secondPassSize) {
            val opToken = secondPassTokens[k] as? Token.OperatorToken ?: break
            val nextToken = secondPassTokens.getOrNull(k + 1) as? Token.NumberToken ?: break

            result = if (opToken.op == '+') {
                result.add(nextToken.value)
            } else if (opToken.op == '-') {
                result.subtract(nextToken.value)
            } else {
                result
            }
            k += 2
        }

        return result
    }

    fun formatNumber(number: BigDecimal): String {
        val stripped = number.stripTrailingZeros()
        val plainString = stripped.toPlainString()

        if (plainString.length > 15) {
            return scientificFormatter.format(number)
        }

        val parts = plainString.split(".")
        val integerPart = parts[0]
        val decimalPart = if (parts.size > 1) parts[1] else null

        val formattedInt = try {
            val bigInt = BigDecimal(integerPart)
            integerFormatter.format(bigInt)
        } catch (e: Exception) {
            integerPart
        }

        return if (decimalPart != null) {
            "$formattedInt.$decimalPart"
        } else {
            formattedInt
        }
    }

    sealed class Result {
        data class Success(val value: String) : Result()
        data class Error(val message: String) : Result()
    }
}
