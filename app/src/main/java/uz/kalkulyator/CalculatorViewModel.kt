package uz.kalkulyator

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.kalkulyator.calc.CalcException
import uz.kalkulyator.calc.ExpressionEvaluator
import uz.kalkulyator.calc.HistoryEntry
import uz.kalkulyator.calc.HistoryStore
import uz.kalkulyator.calc.InputEditor

data class CalculatorUiState(
    val expression: String = "",
    val preview: String = "",
    val error: String? = null,
    val history: List<HistoryEntry> = emptyList(),
    val showHistory: Boolean = false,
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val store = HistoryStore(
        application.getSharedPreferences("kalkulyator", Context.MODE_PRIVATE)
    )

    private val _state = MutableStateFlow(CalculatorUiState(history = store.load()))
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    // After "=", typing a digit starts a new expression while an operator continues the result.
    private var justEvaluated = false

    fun onKey(key: String) {
        val expr = _state.value.expression
        val fresh = if (justEvaluated) "" else expr
        when (key) {
            "C" -> setExpression("")
            "⌫" -> setExpression(InputEditor.backspace(expr))
            "=" -> evaluate()
            "(" -> setExpression(InputEditor.openParen(fresh))
            ")" -> setExpression(InputEditor.closeParen(expr))
            "%" -> setExpression(InputEditor.appendPercent(expr))
            "." -> setExpression(InputEditor.appendDot(fresh))
            else -> {
                val c = key.single()
                if (c in InputEditor.OPERATORS) setExpression(InputEditor.appendOperator(expr, c))
                else setExpression(InputEditor.appendDigit(fresh, c))
            }
        }
    }

    fun toggleHistory() = _state.update { it.copy(showHistory = !it.showHistory) }

    fun clearHistory() {
        store.save(emptyList())
        _state.update { it.copy(history = emptyList()) }
    }

    fun useHistoryEntry(entry: HistoryEntry) {
        setExpression(entry.result)
        justEvaluated = true
        _state.update { it.copy(showHistory = false) }
    }

    private fun setExpression(expr: String) {
        justEvaluated = false
        _state.update { it.copy(expression = expr, preview = preview(expr), error = null) }
    }

    private fun evaluate() {
        val expr = _state.value.expression
        if (expr.isEmpty()) return
        try {
            val result = display(ExpressionEvaluator.evaluate(expr))
            val history = if (result == expr) _state.value.history
            else (listOf(HistoryEntry(expr, result)) + _state.value.history).take(HistoryStore.MAX_ENTRIES)
            store.save(history)
            _state.update { it.copy(expression = result, preview = "", error = null, history = history) }
            justEvaluated = true
        } catch (e: CalcException) {
            _state.update { it.copy(error = e.message) }
        }
    }

    private fun preview(expr: String): String {
        if (expr.none { it in InputEditor.OPERATORS || it == '%' }) return ""
        return try {
            display(ExpressionEvaluator.evaluate(expr)).takeIf { it != expr } ?: ""
        } catch (e: CalcException) {
            ""
        }
    }

    private fun display(value: java.math.BigDecimal) =
        ExpressionEvaluator.format(value).replace('-', '−')
}
