package uz.kalkulyator.calc

import org.junit.Assert.assertEquals
import org.junit.Test

class ExpressionEvaluatorTest {

    private fun calc(expr: String, angle: AngleMode = AngleMode.DEG) =
        ExpressionEvaluator.format(ExpressionEvaluator.evaluate(expr, angle))

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
    @Test fun largeNumberScientific() = assertEquals("1×10^18", calc("1000000000×1000000000"))
    @Test fun smallNumberScientific() = assertEquals("1.5×10^-12", calc("1.5÷1000000000000"))
    @Test fun belowLargeLimitPlain() = assertEquals("123456789012345", calc("123456789012345"))
    @Test fun scientificFormatParsesBack() = assertEquals("2.5×10^20", calc(calc("25×10^19")))

    @Test fun sinDeg() = assertEquals("0.5", calc("sin(30)"))
    @Test fun cosDeg() = assertEquals("0.5", calc("cos(60)"))
    @Test fun sin180IsZero() = assertEquals("0", calc("sin(180)"))
    @Test fun cos90IsZero() = assertEquals("0", calc("cos(90)"))
    @Test fun tanDeg() = assertEquals("1", calc("tan(45)"))
    @Test fun sinRad() = assertEquals("0", calc("sin(π)", AngleMode.RAD))
    @Test fun cosRad() = assertEquals("-1", calc("cos(π)", AngleMode.RAD))
    @Test fun nestedFunctionAutoCloses() = assertEquals("0.5", calc("sin(30"))
    @Test fun functionInExpression() = assertEquals("3", calc("2×sin(30)+2"))

    @Test fun sqrt() = assertEquals("4", calc("√(16)"))
    @Test fun sqrtIrrational() = assertEquals("1.4142135623731", calc("√(2)"))
    @Test fun sqrtWithoutParens() = assertEquals("3", calc("√9"))

    @Test fun power() = assertEquals("1024", calc("2^10"))
    @Test fun negativeExponent() = assertEquals("0.5", calc("2^−1"))
    @Test fun powerBeforeUnaryMinus() = assertEquals("-4", calc("−2^2"))
    @Test fun powerRightAssociative() = assertEquals("512", calc("2^3^2"))
    @Test fun fractionalExponent() = assertEquals("2", calc("4^0.5"))
    @Test fun powerBeforeMultiply() = assertEquals("18", calc("2×3^2"))

    @Test fun log10() = assertEquals("3", calc("log(1000)"))
    @Test fun naturalLog() = assertEquals("1", calc("ln(e)"))
    @Test fun piTimesTwo() = assertEquals("6.28318530717959", calc("2×π"))

    @Test fun tgDeg() = assertEquals("1", calc("tg(45)"))
    @Test fun ctgDeg() = assertEquals("1", calc("ctg(45)"))
    @Test fun ctg90IsZero() = assertEquals("0", calc("ctg(90)"))
    @Test fun arcsinDeg() = assertEquals("30", calc("arcsin(0.5)"))
    @Test fun arccosDeg() = assertEquals("60", calc("arccos(0.5)"))
    @Test fun arccosOne() = assertEquals("0", calc("arccos(1)"))
    @Test fun arctgDeg() = assertEquals("45", calc("arctg(1)"))
    @Test fun arctgNegative() = assertEquals("-45", calc("arctg(−1)"))
    @Test fun arcctgDeg() = assertEquals("45", calc("arcctg(1)"))
    @Test fun arcctgZero() = assertEquals("90", calc("arcctg(0)"))
    @Test fun arcctgNegative() = assertEquals("135", calc("arcctg(−1)"))
    @Test fun arcsinRad() = assertEquals("1.5707963267949", calc("arcsin(1)", AngleMode.RAD))
    @Test fun arctgRad() = assertEquals("0.785398163397448", calc("arctg(1)", AngleMode.RAD))
    @Test fun sinOfArcsinRoundTrips() = assertEquals("0.3", calc("sin(arcsin(0.3))"))

    @Test fun tan90Fails() = assertFails("tan(90)")
    @Test fun tg90Fails() = assertFails("tg(90)")
    @Test fun ctg0Fails() = assertFails("ctg(0)")
    @Test fun arcsinOutOfRangeFails() = assertFails("arcsin(2)")
    @Test fun arccosOutOfRangeFails() = assertFails("arccos(−1.5)")
    @Test fun sqrtNegativeFails() = assertFails("√(−4)")
    @Test fun logZeroFails() = assertFails("log(0)")
    @Test fun lnNegativeFails() = assertFails("ln(−1)")
    @Test fun zeroToNegativeFails() = assertFails("0^−1")
    @Test fun negativeRootFails() = assertFails("(−8)^0.5")
    @Test fun unknownFunctionFails() = assertFails("foo(2)")
    @Test fun functionWithoutParenFails() = assertFails("sin30")

    @Test fun divideByZero() = assertFails("5÷0")
    @Test fun trailingOperator() = assertFails("2+")
    @Test fun unmatchedCloseParen() = assertFails("2)")
    @Test fun twoDots() = assertFails("1.2.3")
    @Test fun empty() = assertFails("")
}
