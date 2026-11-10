package com.example.calculator.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.calculator.domain.CalculatorState

@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val view = LocalView.current

    val triggerHaptic = remember(view) {
        {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Display Area
            DisplayArea(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Keypad Grid Area
            Keypad(
                onDigit = {
                    triggerHaptic()
                    viewModel.onDigitInput(it)
                },
                onOperator = {
                    triggerHaptic()
                    viewModel.onOperatorInput(it)
                },
                onEquals = {
                    triggerHaptic()
                    viewModel.onEquals()
                },
                onClear = {
                    triggerHaptic()
                    viewModel.onClear()
                },
                onBackspace = {
                    triggerHaptic()
                    viewModel.onBackspace()
                },
                onPercentage = {
                    triggerHaptic()
                    viewModel.onPercentage()
                },
                onToggleSign = {
                    triggerHaptic()
                    viewModel.onToggleSign()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DisplayArea(
    state: CalculatorState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        // Expression Display
        Text(
            text = if (state.expression.isEmpty()) "0" else state.expression,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            textAlign = TextAlign.End
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main Result / Live Preview / Error Display
        val resultText = when {
            state.error != null -> state.error
            state.resultPreview.isNotEmpty() -> state.resultPreview
            state.isFinalResult -> state.expression
            else -> ""
        }

        val textColor = when {
            state.error != null -> MaterialTheme.colorScheme.error
            state.resultPreview.isNotEmpty() -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            else -> MaterialTheme.colorScheme.onBackground
        }

        Text(
            text = resultText,
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold
            ),
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Keypad(
    onDigit: (String) -> Unit,
    onOperator: (String) -> Unit,
    onEquals: () -> Unit,
    onClear: () -> Unit,
    onBackspace: () -> Unit,
    onPercentage: () -> Unit,
    onToggleSign: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonSpacing = 12.dp

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(buttonSpacing)
    ) {
        // Row 1: AC, ±, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(
                text = "AC",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onClear,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "±",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onToggleSign,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "%",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onPercentage,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "÷",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { onOperator("÷") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(text = "7", onClick = { onDigit("7") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "8", onClick = { onDigit("8") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "9", onClick = { onDigit("9") }, modifier = Modifier.weight(1f))
            CalculatorButton(
                text = "×",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { onOperator("×") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: 4, 5, 6, -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(text = "4", onClick = { onDigit("4") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "5", onClick = { onDigit("5") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "6", onClick = { onDigit("6") }, modifier = Modifier.weight(1f))
            CalculatorButton(
                text = "-",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { onOperator("-") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(text = "1", onClick = { onDigit("1") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "2", onClick = { onDigit("2") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = "3", onClick = { onDigit("3") }, modifier = Modifier.weight(1f))
            CalculatorButton(
                text = "+",
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                onClick = { onOperator("+") },
                modifier = Modifier.weight(1f)
            )
        }

        // Row 5: 0, ., ⌫, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalculatorButton(text = "0", onClick = { onDigit("0") }, modifier = Modifier.weight(1f))
            CalculatorButton(text = ".", onClick = { onDigit(".") }, modifier = Modifier.weight(1f))
            CalculatorButton(
                text = "⌫",
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onClick = onBackspace,
                modifier = Modifier.weight(1f)
            )
            CalculatorButton(
                text = "=",
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                onClick = onEquals,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CalculatorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(containerColor)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium
            ),
            color = contentColor
        )
    }
}
