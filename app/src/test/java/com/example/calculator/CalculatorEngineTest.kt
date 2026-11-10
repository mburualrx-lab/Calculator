package com.example.calculator

import com.example.calculator.domain.CalculatorEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculatorEngineTest {

    @Test
    fun testBasicAddition() {
        val result = CalculatorEngine.evaluate("12 + 34")
        assertTrue(result is CalculatorEngine.Result.Success)
        assertEquals("46", (result as CalculatorEngine.Result.Success).value)
    }

    @Test
    fun testBasicSubtraction() {
        val result = CalculatorEngine.evaluate("100 - 45")
        assertTrue(result is CalculatorEngine.Result.Success)
        assertEquals("55", (result as CalculatorEngine.Result.Success).value)
    }

    @Test
    fun testMultiplicationAndPrecedence() {
        val result = CalculatorEngine.evaluate("2 + 3 × 4")
        assertTrue(result is CalculatorEngine.Result.Success)
        assertEquals("14", (result as CalculatorEngine.Result.Success).value)
    }

    @Test
    fun testDivision() {
        val result = CalculatorEngine.evaluate("50 ÷ 4")
        assertTrue(result is CalculatorEngine.Result.Success)
        assertEquals("12.5", (result as CalculatorEngine.Result.Success).value)
    }

    @Test
    fun testPrecisionDecimalAddition() {
        val result = CalculatorEngine.evaluate("0.1 + 0.2")
        assertTrue(result is CalculatorEngine.Result.Success)
        assertEquals("0.3", (result as CalculatorEngine.Result.Success).value)
    }

    @Test
    fun testDivisionByZero() {
        val result = CalculatorEngine.evaluate("10 ÷ 0")
        assertTrue(result is CalculatorEngine.Result.Error)
        assertEquals("Cannot divide by zero", (result as CalculatorEngine.Result.Error).message)
    }

    @Test
    fun testPercentageCalculation() {
        val result = CalculatorEngine.evaluate("500 × 10%")
        assertTrue(result is CalculatorEngine.Result.Success)
        assertEquals("50", (result as CalculatorEngine.Result.Success).value)
    }

    @Test
    fun testPercentageWithAddition() {
        val result = CalculatorEngine.evaluate("200 + 10%")
        assertTrue(result is CalculatorEngine.Result.Success)
        assertEquals("200.1", (result as CalculatorEngine.Result.Success).value)
    }

    @Test
    fun testSanitizeTrailingOperator() {
        val result = CalculatorEngine.evaluate("15 + 5 +")
        assertTrue(result is CalculatorEngine.Result.Success)
        assertEquals("20", (result as CalculatorEngine.Result.Success).value)
    }

    @Test
    fun testLargeNumberFormatting() {
        val result = CalculatorEngine.evaluate("1000000 + 2500")
        assertTrue(result is CalculatorEngine.Result.Success)
        assertEquals("1,002,500", (result as CalculatorEngine.Result.Success).value)
    }
}
