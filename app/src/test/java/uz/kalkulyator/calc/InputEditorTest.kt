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
}
