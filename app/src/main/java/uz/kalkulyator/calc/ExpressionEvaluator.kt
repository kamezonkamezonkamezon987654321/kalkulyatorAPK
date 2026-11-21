package uz.kalkulyator.calc

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs

class CalcException(message: String) : Exception(message)

enum class AngleMode { DEG, RAD }

object ExpressionEvaluator {
    private val CALC_MC = MathContext(34, RoundingMode.HALF_EVEN)
    private val DISPLAY_MC = MathContext(15, RoundingMode.HALF_EVEN)
    private val HUNDRED = BigDecimal(100)
    private val PI = BigDecimal("3.141592653589793238462643383279503")
    private val E = BigDecimal("2.718281828459045235360287471352662")
    private val LARGE = BigDecimal("1e15")
    private val SMALL = BigDecimal("1e-9")

    // Double-based functions return values like 1.2e-16 for sin(180°); treat these as exact zero.
    private const val ZERO_EPS = 1e-12
    private const val MAX_EXACT_POWER = 9999

    // "tan" is kept as an alias of "tg" so expressions saved in older history still evaluate.
    val FUNCTIONS = listOf(
        "sin", "cos", "tg", "ctg", "tan",
        "arcsin", "arccos", "arctg", "arcctg",
        "log", "ln",
    )

    fun evaluate(input: String, angle: AngleMode = AngleMode.DEG): BigDecimal {
        val parser = Parser(normalize(input), angle)
        try {
            val value = parser.parseExpression()
            parser.expectEnd()
            return value
        } catch (e: ArithmeticException) {
            throw CalcException("Hisoblab bo'lmadi")
        }
    }

    // Very large/small results use "m×10^n", which evaluate() can parse back.
    fun format(value: BigDecimal): String {
        val rounded = value.round(DISPLAY_MC).stripTrailingZeros()
        if (rounded.signum() == 0) return "0"
        val absValue = rounded.abs()
        if (absValue < LARGE && absValue >= SMALL) return rounded.toPlainString()
        val exponent = rounded.precision() - rounded.scale() - 1
        val mantissa = rounded.movePointLeft(exponent).stripTrailingZeros().toPlainString()
        return "$mantissa×10^$exponent"
    }

    private fun normalize(s: String): String =
        s.replace('×', '*')
            .replace('÷', '/')
            .replace('−', '-')
            .replace(',', '.')
            .filterNot { it.isWhitespace() }

    private fun fromDouble(d: Double): BigDecimal {
        if (!d.isFinite()) throw CalcException("Hisoblab bo'lmadi")
        return BigDecimal.valueOf(d).round(DISPLAY_MC)
    }

    // Grammar:
    //   expr    = term (('+'|'-') term)*
    //   term    = unary (('*'|'/') unary)*
    //   unary   = ('-'|'+') unary | power
    //   power   = postfix ('^' unary)?          right-associative, binds tighter than unary minus
    //   postfix = primary '%'*
    //   primary = number | constant | '(' expr ')' | func '(' expr ')' | '√' postfix
    // A missing ')' at the end of input is closed implicitly.
    private class Parser(private val s: String, private val angle: AngleMode) {
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
            var left = parseUnary()
            while (pos < s.length) {
                left = when (s[pos]) {
                    '*' -> { pos++; left.multiply(parseUnary(), CALC_MC) }
                    '/' -> {
                        pos++
                        val right = parseUnary()
                        if (right.signum() == 0) throw CalcException("Nolga bo'lish mumkin emas")
                        left.divide(right, CALC_MC)
                    }
                    else -> return left
                }
            }
            return left
        }

        private fun parseUnary(): BigDecimal {
            if (pos < s.length && s[pos] == '-') { pos++; return parseUnary().negate() }
            if (pos < s.length && s[pos] == '+') { pos++; return parseUnary() }
            return parsePower()
        }

        private fun parsePower(): BigDecimal {
            val base = parsePostfix()
            if (pos < s.length && s[pos] == '^') {
                pos++
                return power(base, parseUnary())
            }
            return base
        }

        private fun parsePostfix(): BigDecimal {
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
            return when {
                c == '(' -> { pos++; parseGroupRest() }
                c == '√' -> { pos++; sqrt(parsePostfix()) }
                c in '0'..'9' || c == '.' -> parseNumber()
                c.isLetter() -> parseWord()
                else -> throw CalcException("Kutilmagan belgi: '$c'")
            }
        }

        private fun parseGroupRest(): BigDecimal {
            val value = parseExpression()
            if (pos < s.length) {
                if (s[pos] != ')') throw CalcException("Kutilmagan belgi: '${s[pos]}'")
                pos++
            }
            return value
        }

        private fun parseWord(): BigDecimal {
            val start = pos
            while (pos < s.length && s[pos].isLetter()) pos++
            val word = s.substring(start, pos)
            when (word) {
                "π" -> return PI
                "e" -> return E
            }
            if (word !in FUNCTIONS) throw CalcException("Noma'lum funksiya: $word")
            if (pos >= s.length || s[pos] != '(') throw CalcException("$word dan keyin '(' kerak")
            pos++
            val arg = parseGroupRest()
            return when (word) {
                "sin", "cos", "tg", "ctg", "tan" -> trig(word, arg)
                "arcsin", "arccos", "arctg", "arcctg" -> inverseTrig(word, arg)
                "log" -> logarithm(arg, Math::log10)
                else -> logarithm(arg, Math::log)
            }
        }

        private fun parseNumber(): BigDecimal {
            val start = pos
            while (pos < s.length && (s[pos] in '0'..'9' || s[pos] == '.')) pos++
            val text = s.substring(start, pos)
            if (text == "." || text.count { it == '.' } > 1) throw CalcException("Noto'g'ri son: $text")
            return BigDecimal(text)
        }

        private fun trig(name: String, arg: BigDecimal): BigDecimal {
            val rad = if (angle == AngleMode.DEG) Math.toRadians(arg.toDouble()) else arg.toDouble()
            val result = when (name) {
                "sin" -> Math.sin(rad)
                "cos" -> Math.cos(rad)
                "ctg" -> {
                    if (abs(Math.sin(rad)) < ZERO_EPS) throw CalcException("ctg aniqlanmagan")
                    Math.cos(rad) / Math.sin(rad)
                }
                else -> {
                    if (abs(Math.cos(rad)) < ZERO_EPS) throw CalcException("tg aniqlanmagan")
                    Math.tan(rad)
                }
            }
            return fromDouble(if (abs(result) < ZERO_EPS) 0.0 else result)
        }

        private fun inverseTrig(name: String, arg: BigDecimal): BigDecimal {
            val x = arg.toDouble()
            if ((name == "arcsin" || name == "arccos") && abs(x) > 1.0) {
                throw CalcException("$name uchun son −1 dan 1 gacha bo'lishi kerak")
            }
            val rad = when (name) {
                "arcsin" -> Math.asin(x)
                "arccos" -> Math.acos(x)
                "arctg" -> Math.atan(x)
                else -> Math.PI / 2 - Math.atan(x)
            }
            val result = if (angle == AngleMode.DEG) Math.toDegrees(rad) else rad
            return fromDouble(if (abs(result) < ZERO_EPS) 0.0 else result)
        }

        private fun logarithm(arg: BigDecimal, fn: (Double) -> Double): BigDecimal {
            if (arg.signum() <= 0) throw CalcException("Logarifm faqat musbat son uchun")
            return fromDouble(fn(arg.toDouble()))
        }

        private fun sqrt(arg: BigDecimal): BigDecimal {
            if (arg.signum() < 0) throw CalcException("Manfiy sondan ildiz yo'q")
            return fromDouble(Math.sqrt(arg.toDouble()))
        }

        private fun power(base: BigDecimal, exponent: BigDecimal): BigDecimal {
            val intExp = try { exponent.intValueExact() } catch (e: ArithmeticException) { null }
            if (intExp != null && abs(intExp) <= MAX_EXACT_POWER) {
                if (base.signum() == 0 && intExp < 0) throw CalcException("Nolga bo'lish mumkin emas")
                return base.pow(intExp, CALC_MC)
            }
            val result = Math.pow(base.toDouble(), exponent.toDouble())
            if (result.isNaN()) throw CalcException("Hisoblab bo'lmadi")
            return fromDouble(result)
        }
    }
}
