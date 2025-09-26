package com.spreadsheet.core.formula

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

class FormulaParserTest {

    private val parser = FormulaParser()

    @Test
    fun `should return null for non-formula string`() {
        val result = parser.parse("just text")
        assertEquals(null, result)
    }

    @Test
    fun `should parse a number literal`() {
        val result = parser.parse("=123.5")
        assertEquals(NumberLiteral(123.5), result)
    }

    @Test
    fun `should parse a simple cell reference`() {
        val result = parser.parse("=A1")
        assertEquals(CellRef(0, 0), result)
    }

    @Test
    fun `should parse a complex cell reference`() {
        val result = parser.parse("=BC23")
        assertEquals(CellRef(54, 22), result)
    }

    @Test
    fun `should parse a simple addition formula`() {
        val result = parser.parse("=5+C3")
        val expected = BinaryOperation(
            NumberLiteral(5.0),
            Operator.ADD,
            CellRef(2, 2)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should throw exception for invalid formula`() {
        assertThrows<IllegalArgumentException> {
            parser.parse("=1++2")
        }
    }

    @Test
    fun `should handle operator precedence correctly`() {
        val result = parser.parse("=2+3*4")
        val expected = BinaryOperation(
            NumberLiteral(2.0),
            Operator.ADD,
            BinaryOperation(
                NumberLiteral(3.0),
                Operator.MULTIPLY,
                NumberLiteral(4.0)
            )
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should handle multiple operators with same precedence`() {
        // Should be evaluated left-to-right: (10-2)-3
        val result = parser.parse("=10-2-3")
        val expected = BinaryOperation(
            BinaryOperation(
                NumberLiteral(10.0),
                Operator.SUBTRACT,
                NumberLiteral(2.0)
            ),
            Operator.SUBTRACT,
            NumberLiteral(3.0)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should handle parentheses to override precedence`() {
        val result = parser.parse("=(2+3)*4")
        val expected = BinaryOperation(
            BinaryOperation(
                NumberLiteral(2.0),
                Operator.ADD,
                NumberLiteral(3.0)
            ),
            Operator.MULTIPLY,
            NumberLiteral(4.0)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should parse a function call with no arguments`() {
        val result = parser.parse("=TODAY()")
        val expected = FunctionCall("TODAY", emptyList())
        assertEquals(expected, result)
    }

    @Test
    fun `should parse a function call with one argument`() {
        val result = parser.parse("=SUM(A1)")
        val expected = FunctionCall("SUM", listOf(CellRef(0, 0)))
        assertEquals(expected, result)
    }

    @Test
    fun `should parse a function call with multiple arguments`() {
        val result = parser.parse("=SUM(A1, B2, 10)")
        val expected = FunctionCall("SUM", listOf(
            CellRef(0, 0),
            CellRef(1, 1),
            NumberLiteral(10.0)
        ))
        assertEquals(expected, result)
    }

    @Test
    fun `should parse a function call with a cell range argument`() {
        val result = parser.parse("=SUM(A1:B10)")
        val expected = FunctionCall("SUM", listOf(
            CellRange(CellRef(0, 0), CellRef(1, 9))
        ))
        assertEquals(expected, result)
    }

    @Test
    fun `should parse boolean literals`() {
        assertEquals(BooleanLiteral(true), parser.parse("=TRUE"))
        assertEquals(BooleanLiteral(false), parser.parse("=FALSE"))
    }

    @Test
    fun `should parse comparison operators`() {
        val result = parser.parse("=A1>B2")
        val expected = BinaryOperation(
            CellRef(0, 0),
            Operator.GREATER_THAN,
            CellRef(1, 1)
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should handle precedence of comparison vs addition`() {
        // Should be parsed as (A1+B1) > C1
        val result = parser.parse("=A1+B1>C1")
        val expected = BinaryOperation(
            BinaryOperation(
                CellRef(0, 0), // A1
                Operator.ADD,
                CellRef(1, 0)  // B1
            ),
            Operator.GREATER_THAN,
            CellRef(2, 0) // C1
        )
        assertEquals(expected, result)
    }

    @Test
    fun `should parse a named range`() {
        val result = parser.parse("=MyRange")
        assertEquals(NamedRange("MYRANGE"), result)
    }
}
