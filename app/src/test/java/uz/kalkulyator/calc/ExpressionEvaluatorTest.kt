package uz.kalkulyator.calc

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpressionEvaluatorTest {

    private fun calc(expr: String) = ExpressionEvaluator.format(ExpressionEvaluator.evaluate(expr))

    private fun assertFails(expr: String) {
        try {
            ExpressionEvaluator.evaluate(expr)
            throw AssertionError("'$expr' xato berishi kerak edi")
        } catch (e: CalcException) {
            // expected
        }
    }

    @Test fun precedence() = assertEquals("14", calc("2+3×4"))
    @Test fun parentheses() = assertEquals("20", calc("(2+3)×4"))
    @Test fun decimalsAreExact() = assertEquals("0.3", calc("0.1+0.2"))
    @Test fun division() = assertEquals("2.5", calc("10÷4"))
    @Test fun repeatingDivisionRounded() = assertEquals("0.333333333333333", calc("1÷3"))
    @Test fun unaryMinus() = assertEquals("-3", calc("−5+2"))
    @Test fun doubleNegation() = assertEquals("2", calc("−(−2)"))
    @Test fun multiplyByNegative() = assertEquals("-15", calc("5×−3"))
    @Test fun percent() = assertEquals("0.5", calc("50%"))
    @Test fun percentOfValue() = assertEquals("20", calc("200×10%"))
    @Test fun unclosedParenAutoCloses() = assertEquals("5", calc("(2+3"))
    @Test fun trailingDot() = assertEquals("5", calc("5.+0"))
    @Test fun zeroResult() = assertEquals("0", calc("2−2"))
    @Test fun largeNumberPlain() = assertEquals("1000000000000000000", calc("1000000000×1000000000"))

    @Test fun divideByZero() = assertFails("5÷0")
    @Test fun trailingOperator() = assertFails("2+")
    @Test fun unmatchedCloseParen() = assertFails("2)")
    @Test fun twoDots() = assertFails("1.2.3")
    @Test fun empty() = assertFails("")
}
