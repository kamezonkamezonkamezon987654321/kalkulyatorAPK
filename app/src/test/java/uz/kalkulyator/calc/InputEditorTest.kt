package uz.kalkulyator.calc

import org.junit.Assert.assertEquals
import org.junit.Test

class InputEditorTest {

    @Test fun leadingZeroReplaced() = assertEquals("7", InputEditor.appendDigit("0", '7'))
    @Test fun zeroAfterOperatorReplaced() = assertEquals("5+7", InputEditor.appendDigit("5+0", '7'))
    @Test fun digitAfterParenMultiplies() = assertEquals("(2)×3", InputEditor.appendDigit("(2)", '3'))

    @Test fun dotOnEmptyAddsZero() = assertEquals("0.", InputEditor.appendDot(""))
    @Test fun secondDotIgnored() = assertEquals("1.5", InputEditor.appendDot("1.5"))
    @Test fun dotAfterOperator() = assertEquals("1+0.", InputEditor.appendDot("1+"))

    @Test fun operatorReplacesOperator() = assertEquals("5×", InputEditor.appendOperator("5+", '×'))
    @Test fun minusAfterMultiply() = assertEquals("5×−", InputEditor.appendOperator("5×", '−'))
    @Test fun operatorReplacesSignedTail() = assertEquals("5+", InputEditor.appendOperator("5×−", '+'))
    @Test fun leadingMinusAllowed() = assertEquals("−", InputEditor.appendOperator("", '−'))
    @Test fun leadingPlusIgnored() = assertEquals("", InputEditor.appendOperator("", '+'))
    @Test fun minusAfterOpenParen() = assertEquals("(−", InputEditor.appendOperator("(", '−'))
    @Test fun timesAfterOpenParenIgnored() = assertEquals("(−", InputEditor.appendOperator("(−", '×'))

    @Test fun percentAfterDigit() = assertEquals("50%", InputEditor.appendPercent("50"))
    @Test fun percentAfterOperatorIgnored() = assertEquals("5+", InputEditor.appendPercent("5+"))

    @Test fun openParenAfterDigitMultiplies() = assertEquals("2×(", InputEditor.openParen("2"))
    @Test fun openParenAfterOperator() = assertEquals("2+(", InputEditor.openParen("2+"))
    @Test fun closeParenWhenOpen() = assertEquals("(2+3)", InputEditor.closeParen("(2+3"))
    @Test fun closeParenWithoutOpenIgnored() = assertEquals("2", InputEditor.closeParen("2"))
    @Test fun closeParenAfterOperatorIgnored() = assertEquals("(2+", InputEditor.closeParen("(2+"))

    @Test fun backspace() = assertEquals("12", InputEditor.backspace("123"))
    @Test fun backspaceRemovesWholeFunction() = assertEquals("5+", InputEditor.backspace("5+sin("))
    @Test fun backspaceRemovesSqrt() = assertEquals("2×", InputEditor.backspace("2×√("))
    @Test fun backspaceRemovesArcsinWhole() = assertEquals("2×", InputEditor.backspace("2×arcsin("))
    @Test fun backspaceRemovesArcctgWhole() = assertEquals("", InputEditor.backspace("arcctg("))
    @Test fun backspaceRemovesCtgWhole() = assertEquals("1+", InputEditor.backspace("1+ctg("))

    @Test fun functionOnEmpty() = assertEquals("sin(", InputEditor.appendFunction("", "sin"))
    @Test fun functionAfterDigitMultiplies() = assertEquals("2×cos(", InputEditor.appendFunction("2", "cos"))
    @Test fun functionAfterOperator() = assertEquals("2+ln(", InputEditor.appendFunction("2+", "ln"))
    @Test fun sqrtAfterOpenParen() = assertEquals("(√(", InputEditor.appendFunction("(", "√"))

    @Test fun constantAfterDigitMultiplies() = assertEquals("2×π", InputEditor.appendConstant("2", 'π'))
    @Test fun constantOnEmpty() = assertEquals("e", InputEditor.appendConstant("", 'e'))
    @Test fun digitAfterConstantMultiplies() = assertEquals("π×2", InputEditor.appendDigit("π", '2'))

    @Test fun square() = assertEquals("3^2", InputEditor.appendSquare("3"))
    @Test fun squareAfterOperatorIgnored() = assertEquals("3+", InputEditor.appendSquare("3+"))
    @Test fun negativeExponentAllowed() = assertEquals("5^−", InputEditor.appendOperator("5^", '−'))
    @Test fun powerReplacesOperator() = assertEquals("5^", InputEditor.appendOperator("5+", '^'))
    @Test fun closeParenAfterPowerIgnored() = assertEquals("(5^", InputEditor.closeParen("(5^"))
}
