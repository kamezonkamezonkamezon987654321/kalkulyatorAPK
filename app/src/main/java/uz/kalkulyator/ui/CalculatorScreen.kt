package uz.kalkulyator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import uz.kalkulyator.CalculatorUiState
import uz.kalkulyator.CalculatorViewModel
import uz.kalkulyator.calc.HistoryEntry

private val KEY_ROWS = listOf(
    listOf("C", "(", ")", "÷"),
    listOf("7", "8", "9", "×"),
    listOf("4", "5", "6", "−"),
    listOf("1", "2", "3", "+"),
    listOf("%", "0", ".", "⌫"),
)

@Composable
fun CalculatorScreen(vm: CalculatorViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    Scaffold { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = vm::toggleHistory) {
                    Text(if (state.showHistory) "← Kalkulyator" else "Tarix (${state.history.size})")
                }
                if (state.showHistory && state.history.isNotEmpty()) {
                    TextButton(onClick = vm::clearHistory) { Text("Tozalash") }
                }
            }
            if (state.showHistory) {
                HistoryList(state.history, vm::useHistoryEntry, Modifier.weight(1f))
            } else {
                Display(state, Modifier.weight(1f))
                Keypad(vm::onKey)
            }
        }
    }
}

@Composable
private fun Display(state: CalculatorUiState, modifier: Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End,
    ) {
        Text(
            text = state.expression.ifEmpty { "0" },
            fontSize = if (state.expression.length > 12) 32.sp else 48.sp,
            lineHeight = if (state.expression.length > 12) 38.sp else 56.sp,
            textAlign = TextAlign.End,
            maxLines = 3,
        )
        val secondary = state.error ?: state.preview.takeIf { it.isNotEmpty() }?.let { "= $it" } ?: ""
        Text(
            text = secondary,
            fontSize = 24.sp,
            color = if (state.error != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 2,
        )
    }
}

@Composable
private fun Keypad(onKey: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        KEY_ROWS.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { key ->
                    val (bg, fg) = when (key) {
                        "C" -> colors.errorContainer to colors.onErrorContainer
                        "÷", "×", "−", "+" -> colors.primaryContainer to colors.onPrimaryContainer
                        "(", ")", "%", "⌫" -> colors.secondaryContainer to colors.onSecondaryContainer
                        else -> colors.surfaceVariant to colors.onSurfaceVariant
                    }
                    KeyButton(key, bg, fg, Modifier.weight(1f)) { onKey(key) }
                }
            }
        }
        KeyButton("=", colors.primary, colors.onPrimary, Modifier.fillMaxWidth()) { onKey("=") }
    }
}

@Composable
private fun KeyButton(label: String, bg: Color, fg: Color, modifier: Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = fg),
    ) {
        Text(label, fontSize = 26.sp)
    }
}

@Composable
private fun HistoryList(history: List<HistoryEntry>, onPick: (HistoryEntry) -> Unit, modifier: Modifier) {
    if (history.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tarix bo'sh", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }
    LazyColumn(modifier.fillMaxWidth()) {
        items(history) { entry ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .clickable { onPick(entry) }
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.End,
            ) {
                Text(entry.expression, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("= ${entry.result}", fontSize = 26.sp)
            }
            HorizontalDivider()
        }
    }
}
