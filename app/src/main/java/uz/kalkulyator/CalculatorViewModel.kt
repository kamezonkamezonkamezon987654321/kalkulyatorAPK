package uz.kalkulyator

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import uz.kalkulyator.calc.AngleMode
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
    val scientific: Boolean = false,
    val inverse: Boolean = false,
    val angleMode: AngleMode = AngleMode.DEG,
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kalkulyator", Context.MODE_PRIVATE)
    private val store = HistoryStore(prefs)

    private val _state = MutableStateFlow(
        CalculatorUiState(
            history = store.load(),
            scientific = prefs.getBoolean(KEY_SCIENTIFIC, false),
            angleMode = if (prefs.getBoolean(KEY_RADIANS, false)) AngleMode.RAD else AngleMode.DEG,
        )
    )
    val state: StateFlow<CalculatorUiState> = _state.asStateFlow()

    // After "=", typing a value starts a new expression while an operator continues the result.
    private var justEvaluated = false

    fun onKey(key: String) {
        val expr = _state.value.expression
        val fresh = if (justEvaluated) "" else expr
        val next = when (key) {
            "C" -> ""
            "⌫" -> InputEditor.backspace(expr)
            "=" -> { evaluate(); return }
            "inv" -> { _state.update { it.copy(inverse = !it.inverse) }; return }
            "(" -> InputEditor.openParen(fresh)
            ")" -> InputEditor.closeParen(expr)
            "%" -> InputEditor.appendPercent(expr)
            "." -> InputEditor.appendDot(fresh)
            "xʸ" -> InputEditor.appendOperator(expr, '^')
            "x²" -> InputEditor.appendSquare(expr)
            "√" -> InputEditor.appendFunction(fresh, "√")
            "π", "e" -> InputEditor.appendConstant(fresh, key.single())
            in ExpressionEvaluator.FUNCTIONS -> InputEditor.appendFunction(fresh, key)
            else -> {
                val c = key.single()
                if (c in InputEditor.OPERATORS) InputEditor.appendOperator(expr, c)
                else InputEditor.appendDigit(fresh, c)
            }
        }
        setExpression(next)
    }

    fun toggleHistory() = _state.update { it.copy(showHistory = !it.showHistory) }

    fun toggleScientific() {
        val scientific = !_state.value.scientific
        prefs.edit().putBoolean(KEY_SCIENTIFIC, scientific).apply()
        _state.update { it.copy(scientific = scientific) }
    }

    fun toggleAngleMode() {
        val mode = if (_state.value.angleMode == AngleMode.DEG) AngleMode.RAD else AngleMode.DEG
        prefs.edit().putBoolean(KEY_RADIANS, mode == AngleMode.RAD).apply()
        _state.update { it.copy(angleMode = mode, preview = preview(it.expression, mode)) }
    }

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
        _state.update { it.copy(expression = expr, preview = preview(expr, it.angleMode), error = null) }
    }

    private fun evaluate() {
        val current = _state.value
        val expr = current.expression
        if (expr.isEmpty()) return
        try {
            val result = display(ExpressionEvaluator.evaluate(expr, current.angleMode))
            val history = if (result == expr) current.history
            else (listOf(HistoryEntry(expr, result)) + current.history).take(HistoryStore.MAX_ENTRIES)
            store.save(history)
            _state.update { it.copy(expression = result, preview = "", error = null, history = history) }
            justEvaluated = true
        } catch (e: CalcException) {
            _state.update { it.copy(error = e.message) }
        }
    }

    private fun preview(expr: String, angle: AngleMode): String {
        if (expr.all { it in '0'..'9' || it == '.' }) return ""
        return try {
            display(ExpressionEvaluator.evaluate(expr, angle)).takeIf { it != expr } ?: ""
        } catch (e: CalcException) {
            ""
        }
    }

    private fun display(value: BigDecimal) = ExpressionEvaluator.format(value).replace('-', '−')

    private companion object {
        const val KEY_SCIENTIFIC = "scientific"
        const val KEY_RADIANS = "radians"
    }
}
