package uz.kalkulyator.calc

object InputEditor {
    const val OPERATORS = "+−×÷^"
    private const val CONSTANTS = "πe"
    // Longest first, so "arcsin(" is removed whole instead of matching its "sin(" suffix.
    private val FUNCTION_TOKENS =
        (ExpressionEvaluator.FUNCTIONS.map { "$it(" } + "√(").sortedByDescending { it.length }

    fun appendDigit(expr: String, digit: Char): String {
        if (endsWithValueCloser(expr)) return expr + "×" + digit
        if (currentNumber(expr) == "0") return expr.dropLast(1) + digit
        return expr + digit
    }

    fun appendDot(expr: String): String {
        if (endsWithValueCloser(expr)) return expr + "×0."
        val number = currentNumber(expr)
        return when {
            number.contains('.') -> expr
            number.isEmpty() -> expr + "0."
            else -> "$expr."
        }
    }

    fun appendOperator(expr: String, op: Char): String {
        val base = expr.trimEnd { it in OPERATORS }
        if (base.isEmpty() || base.endsWith("(")) {
            return if (op == '−') base + op else expr
        }
        if (op == '−' && expr.last() in "×÷^") return expr + op
        return base + op
    }

    fun appendPercent(expr: String): String = if (endsWithValue(expr)) "$expr%" else expr

    fun appendSquare(expr: String): String = if (endsWithValue(expr)) "$expr^2" else expr

    // name is a function name like "sin", or "√"
    fun appendFunction(expr: String, name: String): String =
        if (startsNewValue(expr)) "$expr$name(" else "$expr×$name("

    fun appendConstant(expr: String, constant: Char): String =
        if (startsNewValue(expr)) "$expr$constant" else "$expr×$constant"

    fun openParen(expr: String): String = if (startsNewValue(expr)) "$expr(" else "$expr×("

    fun closeParen(expr: String): String {
        val open = expr.count { it == '(' } - expr.count { it == ')' }
        val last = expr.lastOrNull() ?: return expr
        return if (open > 0 && last !in OPERATORS && last != '(') "$expr)" else expr
    }

    fun backspace(expr: String): String {
        val token = FUNCTION_TOKENS.firstOrNull { expr.endsWith(it) }
        return if (token != null) expr.dropLast(token.length) else expr.dropLast(1)
    }

    private fun startsNewValue(expr: String): Boolean {
        val last = expr.lastOrNull() ?: return true
        return last in OPERATORS || last == '('
    }

    private fun endsWithValue(expr: String): Boolean {
        val last = expr.lastOrNull() ?: return false
        return last in '0'..'9' || last == ')' || last == '.' || last == '%' || last in CONSTANTS
    }

    private fun endsWithValueCloser(expr: String): Boolean {
        val last = expr.lastOrNull() ?: return false
        return last == ')' || last == '%' || last in CONSTANTS
    }

    private fun currentNumber(expr: String) = expr.takeLastWhile { it in '0'..'9' || it == '.' }
}
