package uz.kalkulyator.calc

object InputEditor {
    const val OPERATORS = "+−×÷"

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
        if (op == '−' && expr.last() in "×÷") return expr + op
        return base + op
    }

    fun appendPercent(expr: String): String {
        val last = expr.lastOrNull() ?: return expr
        return if (last in '0'..'9' || last == ')' || last == '.') "$expr%" else expr
    }

    fun openParen(expr: String): String {
        val last = expr.lastOrNull()
        return if (last == null || last in OPERATORS || last == '(') "$expr(" else "$expr×("
    }

    fun closeParen(expr: String): String {
        val open = expr.count { it == '(' } - expr.count { it == ')' }
        val last = expr.lastOrNull() ?: return expr
        return if (open > 0 && last !in OPERATORS && last != '(') "$expr)" else expr
    }

    fun backspace(expr: String): String = expr.dropLast(1)

    private fun endsWithValueCloser(expr: String) = expr.endsWith(")") || expr.endsWith("%")

    private fun currentNumber(expr: String) = expr.takeLastWhile { it in '0'..'9' || it == '.' }
}
