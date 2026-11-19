package uz.kalkulyator.calc

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

class CalcException(message: String) : Exception(message)

object ExpressionEvaluator {
    private val CALC_MC = MathContext(34, RoundingMode.HALF_EVEN)
    private val DISPLAY_MC = MathContext(15, RoundingMode.HALF_EVEN)
    private val HUNDRED = BigDecimal(100)

    fun evaluate(input: String): BigDecimal {
        val parser = Parser(normalize(input))
        try {
            val value = parser.parseExpression()
            parser.expectEnd()
            return value
        } catch (e: ArithmeticException) {
            throw CalcException("Hisoblab bo'lmadi")
        }
    }

    fun format(value: BigDecimal): String {
        val rounded = value.round(DISPLAY_MC).stripTrailingZeros()
        return if (rounded.signum() == 0) "0" else rounded.toPlainString()
    }

    private fun normalize(s: String): String =
        s.replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace(',', '.')
            .filterNot { it.isWhitespace() }

    // Grammar: expr = term (('+'|'-') term)* ; term = factor (('*'|'/') factor)* ;
    // factor = ('-'|'+') factor | primary '%'* ; primary = number | '(' expr ')'?
    // A missing ')' at the end of input is closed implicitly.
    private class Parser(private val s: String) {
        private var pos = 0

        fun expectEnd() {
            if (pos < s.length) throw CalcException("Kutilmagan belgi: '${s[pos]}'")
        }

        fun parseExpression(): BigDecimal {
            var left = parseTerm()
            while (pos < s.length) {
                left = when (s[pos]) {
                    '+' -> { pos++; left.add(parseTerm(), CALC_MC) }
                    '-' -> { pos++; left.subtract(parseTerm(), CALC_MC) }
                    else -> return left
                }
            }
            return left
        }

        private fun parseTerm(): BigDecimal {
            var left = parseFactor()
            while (pos < s.length) {
                left = when (s[pos]) {
                    '*' -> { pos++; left.multiply(parseFactor(), CALC_MC) }
                    '/' -> {
                        pos++
                        val right = parseFactor()
                        if (right.signum() == 0) throw CalcException("Nolga bo'lish mumkin emas")
                        left.divide(right, CALC_MC)
                    }
                    else -> return left
                }
            }
            return left
        }

        private fun parseFactor(): BigDecimal {
            if (pos < s.length && s[pos] == '-') { pos++; return parseFactor().negate() }
            if (pos < s.length && s[pos] == '+') { pos++; return parseFactor() }
            var value = parsePrimary()
            while (pos < s.length && s[pos] == '%') {
                pos++
                value = value.divide(HUNDRED, CALC_MC)
            }
            return value
        }

        private fun parsePrimary(): BigDecimal {
            if (pos >= s.length) throw CalcException("Ifoda tugallanmagan")
            val c = s[pos]
            if (c == '(') {
                pos++
                val value = parseExpression()
                if (pos < s.length) {
                    if (s[pos] != ')') throw CalcException("Kutilmagan belgi: '${s[pos]}'")
                    pos++
                }
                return value
            }
            if (c in '0'..'9' || c == '.') return parseNumber()
            throw CalcException("Kutilmagan belgi: '$c'")
        }

        private fun parseNumber(): BigDecimal {
            val start = pos
            while (pos < s.length && (s[pos] in '0'..'9' || s[pos] == '.')) pos++
            val text = s.substring(start, pos)
            if (text == "." || text.count { it == '.' } > 1) throw CalcException("Noto'g'ri son: $text")
            return BigDecimal(text)
        }
    }
}
