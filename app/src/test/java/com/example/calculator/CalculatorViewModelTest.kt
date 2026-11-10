package com.example.calculator

import com.example.calculator.ui.CalculatorViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculatorViewModelTest {

    private lateinit var viewModel: CalculatorViewModel

    @Before
    fun setUp() {
        viewModel = CalculatorViewModel()
    }

    @Test
    fun testDigitInputAndLivePreview() {
        viewModel.onDigitInput("1")
        viewModel.onDigitInput("2")
        viewModel.onOperatorInput("+")
        viewModel.onDigitInput("8")

        val state = viewModel.uiState.value
        assertEquals("12 + 8", state.expression)
        assertEquals("20", state.resultPreview)
        assertFalse(state.isFinalResult)
    }

    @Test
    fun testEqualsExecution() {
        viewModel.onDigitInput("1")
        viewModel.onDigitInput("5")
        viewModel.onOperatorInput("×")
        viewModel.onDigitInput("4")
        viewModel.onEquals()

        val state = viewModel.uiState.value
        assertEquals("60", state.expression)
        assertEquals("", state.resultPreview)
        assertTrue(state.isFinalResult)
    }

    @Test
    fun testClearOperation() {
        viewModel.onDigitInput("9")
        viewModel.onOperatorInput("+")
        viewModel.onDigitInput("9")
        viewModel.onClear()

        val state = viewModel.uiState.value
        assertEquals("", state.expression)
        assertEquals("", state.resultPreview)
        assertNull(state.error)
    }

    @Test
    fun testBackspaceOperation() {
        viewModel.onDigitInput("1")
        viewModel.onDigitInput("2")
        viewModel.onDigitInput("3")
        viewModel.onBackspace()

        val state = viewModel.uiState.value
        assertEquals("12", state.expression)
    }

    @Test
    fun testDivisionByZeroErrorState() {
        viewModel.onDigitInput("5")
        viewModel.onOperatorInput("÷")
        viewModel.onDigitInput("0")
        viewModel.onEquals()

        val state = viewModel.uiState.value
        assertEquals("Cannot divide by zero", state.error)
        assertTrue(state.isFinalResult)
    }
}
