package com.spreadsheet.core.spreadsheet

import com.spreadsheet.core.formula.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach

class SheetTest {

    private lateinit var sheet: Sheet

    @BeforeEach
    fun setUp() {
        sheet = Sheet()
    }

    @Test
    fun `should store number literal correctly`() {
        sheet.updateCell(0, 0, "123.5")
        val expected = Cell(
            rawValue = "123.5",
            computedValue = CellValue.DoubleValue(123.5),
            parsedExpression = null
        )
        assertEquals(expected, sheet.getCell(0, 0))
    }

    @Test
    fun `should store string literal correctly`() {
        sheet.updateCell(0, 0, "Hello")
        val expected = Cell(
            rawValue = "Hello",
            computedValue = CellValue.StringValue("Hello"),
            parsedExpression = null
        )
        assertEquals(expected, sheet.getCell(0, 0))
    }

    @Test
    fun `should parse and evaluate a simple numeric formula`() {
        sheet.updateCell(0, 0, "=5")
        val expected = Cell(
            rawValue = "=5",
            computedValue = CellValue.DoubleValue(5.0),
            parsedExpression = NumberLiteral(5.0)
        )
        assertEquals(expected, sheet.getCell(0, 0))
    }

    @Test
    fun `should evaluate simple addition formula`() {
        sheet.updateCell(0, 0, "=1+2")
        val cell = sheet.getCell(0, 0)
        assertEquals(CellValue.DoubleValue(3.0), cell?.computedValue)
    }

    @Test
    fun `should evaluate formula with cell references`() {
        // Set up prerequisite cells
        sheet.updateCell(0, 0, "10")  // A1
        sheet.updateCell(0, 1, "20")  // B1

        // Set up formula cell
        sheet.updateCell(0, 2, "=A1+B1") // C1

        val cell = sheet.getCell(0, 2)
        assertEquals(CellValue.DoubleValue(30.0), cell?.computedValue)
    }

    @Test
    fun `should treat empty cell as zero in formula`() {
        sheet.updateCell(0, 0, "10") // A1
        // B1 is empty

        sheet.updateCell(0, 2, "=A1+B1") // C1
        val cell = sheet.getCell(0, 2)
        assertEquals(CellValue.DoubleValue(10.0), cell?.computedValue)
    }

    @Test
    fun `should return value error if operand is a string`() {
        sheet.updateCell(0, 0, "10")      // A1
        sheet.updateCell(0, 1, "hello") // B1

        sheet.updateCell(0, 2, "=A1+B1") // C1
        val cell = sheet.getCell(0, 2)
        assertEquals(CellValue.ErrorValue("#VALUE!"), cell?.computedValue)
    }

    @Test
    fun `should return error for invalid formula syntax`() {
        sheet.updateCell(0, 0, "=1++2")
        val expected = Cell(
            rawValue = "=1++2",
            computedValue = CellValue.ErrorValue("#ERROR!"),
            parsedExpression = null
        )
        assertEquals(expected, sheet.getCell(0, 0))
    }

    @Test
    fun `should correctly evaluate formula with operator precedence`() {
        sheet.updateCell(0, 0, "=2+3*4")
        assertEquals(CellValue.DoubleValue(14.0), sheet.getCell(0, 0)?.computedValue)
    }

    @Test
    fun `should correctly evaluate formula with multiple operators`() {
        sheet.updateCell(0, 0, "=10-2*3+8/4") // 10 - 6 + 2 = 6
        assertEquals(CellValue.DoubleValue(6.0), sheet.getCell(0, 0)?.computedValue)
    }

    @Test
    fun `should handle division by zero`() {
        sheet.updateCell(0, 0, "=10/0")
        assertEquals(CellValue.ErrorValue("#DIV/0!"), sheet.getCell(0, 0)?.computedValue)
    }

    @Test
    fun `should correctly evaluate formula with parentheses`() {
        sheet.updateCell(0, 0, "=(2+3)*4")
        assertEquals(CellValue.DoubleValue(20.0), sheet.getCell(0, 0)?.computedValue)
    }

    @Test
    fun `should correctly track precedents and dependents`() {
        // C1 = A1 + B1
        sheet.updateCell(0, 0, "10") // A1
        sheet.updateCell(0, 1, "20") // B1
        sheet.updateCell(0, 2, "=A1+B1") // C1

        val c1Pos = 0 to 2
        val a1Pos = 0 to 0
        val b1Pos = 0 to 1

        assertEquals(setOf(a1Pos, b1Pos), sheet.getPrecedents(c1Pos.first, c1Pos.second))
        assertEquals(setOf(c1Pos), sheet.getDependents(a1Pos.first, a1Pos.second))
        assertEquals(setOf(c1Pos), sheet.getDependents(b1Pos.first, b1Pos.second))
    }

    @Test
    fun `should update dependencies when formula changes`() {
        // 1. C1 = A1 + B1
        sheet.updateCell(0, 0, "10") // A1
        sheet.updateCell(0, 1, "20") // B1
        sheet.updateCell(0, 2, "=A1+B1") // C1

        // 2. Change C1 to = A1 + 5
        sheet.updateCell(0, 2, "=A1+5")

        val c1Pos = 0 to 2
        val a1Pos = 0 to 0
        val b1Pos = 0 to 1

        // C1 should now only depend on A1
        assertEquals(setOf(a1Pos), sheet.getPrecedents(c1Pos.first, c1Pos.second))
        // A1 should still have C1 as a dependent
        assertEquals(setOf(c1Pos), sheet.getDependents(a1Pos.first, a1Pos.second))
        // B1 should no longer have C1 as a dependent
        assertEquals(null, sheet.getDependents(b1Pos.first, b1Pos.second))
    }

    @Test
    fun `should clear dependencies when formula is replaced with a value`() {
        // 1. C1 = A1 + B1
        sheet.updateCell(0, 0, "10") // A1
        sheet.updateCell(0, 1, "20") // B1
        sheet.updateCell(0, 2, "=A1+B1") // C1

        // 2. Change C1 to a static value
        sheet.updateCell(0, 2, "100")

        val c1Pos = 0 to 2
        val a1Pos = 0 to 0
        val b1Pos = 0 to 1

        // C1 should have no precedents
        assertEquals(null, sheet.getPrecedents(c1Pos.first, c1Pos.second))
        // A1 and B1 should have no dependents
        assertEquals(null, sheet.getDependents(a1Pos.first, a1Pos.second))
        assertEquals(null, sheet.getDependents(b1Pos.first, b1Pos.second))
    }

    @Test
    fun `should recalculate dependent cells when a precedent changes`() {
        // C1 = A1 + B1
        sheet.updateCell(0, 0, "10") // A1
        sheet.updateCell(0, 1, "20") // B1
        sheet.updateCell(0, 2, "=A1+B1") // C1

        assertEquals(CellValue.DoubleValue(30.0), sheet.getCell(0, 2)?.computedValue)

        // Now, update A1. C1 should be recalculated automatically.
        sheet.updateCell(0, 0, "15")

        assertEquals(CellValue.DoubleValue(35.0), sheet.getCell(0, 2)?.computedValue)
    }

    @Test
    fun `should handle chained recalculations`() {
        sheet.updateCell(0, 0, "10") // A1
        sheet.updateCell(0, 1, "=A1*2") // B1
        sheet.updateCell(0, 2, "=B1+5") // C1

        assertEquals(CellValue.DoubleValue(20.0), sheet.getCell(0, 1)?.computedValue)
        assertEquals(CellValue.DoubleValue(25.0), sheet.getCell(0, 2)?.computedValue)

        // Update A1, which should trigger B1, which should trigger C1
        sheet.updateCell(0, 0, "5")

        assertEquals(CellValue.DoubleValue(10.0), sheet.getCell(0, 1)?.computedValue)
        assertEquals(CellValue.DoubleValue(15.0), sheet.getCell(0, 2)?.computedValue)
    }

    @Test
    fun `should detect and block direct circular reference`() {
        sheet.updateCell(0, 0, "=A1")
        assertEquals(CellValue.ErrorValue("#REF!"), sheet.getCell(0, 0)?.computedValue)
    }

    @Test
    fun `should detect and block indirect circular reference`() {
        sheet.updateCell(0, 0, "=B1") // A1 = B1
        sheet.updateCell(0, 1, "=A1") // B1 = A1

        // The second update should fail with a #REF! error
        assertEquals(CellValue.ErrorValue("#REF!"), sheet.getCell(0, 1)?.computedValue)
    }

    @Test
    fun `should evaluate SUM function with a range`() {
        sheet.updateCell(0, 0, "10") // A1
        sheet.updateCell(1, 0, "20") // A2
        sheet.updateCell(2, 0, "30") // A3
        sheet.updateCell(3, 0, "text") // A4 - should be ignored
        
        sheet.updateCell(0, 1, "=SUM(A1:A4)") // B1
        assertEquals(CellValue.DoubleValue(60.0), sheet.getCell(0, 1)?.computedValue)
    }

    @Test
    fun `should evaluate AVERAGE function with multiple arguments`() {
        sheet.updateCell(0, 0, "10") // A1
        sheet.updateCell(0, 1, "20") // B1
        
        sheet.updateCell(0, 2, "=AVERAGE(A1, B1, 30)") // C1
        assertEquals(CellValue.DoubleValue(20.0), sheet.getCell(0, 2)?.computedValue)
    }

    @Test
    fun `should evaluate COUNT function`() {
        sheet.updateCell(0, 0, "10") // A1
        sheet.updateCell(0, 1, "text") // B1
        sheet.updateCell(0, 2, "") // C1 (empty)

        sheet.updateCell(0, 3, "=COUNT(A1:C1)") // D1
        assertEquals(CellValue.DoubleValue(1.0), sheet.getCell(0, 3)?.computedValue)
    }
    
    @Test
    fun `should evaluate MAX and MIN functions`() {
        sheet.updateCell(0, 0, "10")
        sheet.updateCell(0, 1, "100")
        sheet.updateCell(0, 2, "-5")

        sheet.updateCell(1, 0, "=MAX(A1:C1)")
        assertEquals(CellValue.DoubleValue(100.0), sheet.getCell(1, 0)?.computedValue)
        
        sheet.updateCell(1, 1, "=MIN(A1:C1)")
        assertEquals(CellValue.DoubleValue(-5.0), sheet.getCell(1, 1)?.computedValue)
    }

    @Test
    fun `should return name error for unknown function`() {
        // Use B1 to avoid a circular reference error, which is checked first
        sheet.updateCell(0, 0, "=UNKNOWNFUNC(B1)")
        assertEquals(CellValue.ErrorValue("#NAME?"), sheet.getCell(0, 0)?.computedValue)
    }

    @Test
    fun `should return ref error for function with circular self-reference`() {
        sheet.updateCell(0, 0, "=SUM(A1)")
        assertEquals(CellValue.ErrorValue("#REF!"), sheet.getCell(0, 0)?.computedValue)
    }

    @Test
    fun `should evaluate boolean literals`() {
        sheet.updateCell(0, 0, "=TRUE")
        assertEquals(CellValue.BooleanValue(true), sheet.getCell(0, 0)?.computedValue)
        sheet.updateCell(0, 1, "=FALSE")
        assertEquals(CellValue.BooleanValue(false), sheet.getCell(0, 1)?.computedValue)
    }

    @Test
    fun `should evaluate comparison formulas`() {
        sheet.updateCell(0, 0, "10")
        sheet.updateCell(0, 1, "20")
        
        sheet.updateCell(1, 0, "=A1>B1") // false
        assertEquals(CellValue.BooleanValue(false), sheet.getCell(1, 0)?.computedValue)

        sheet.updateCell(1, 1, "=A1<B1") // true
        assertEquals(CellValue.BooleanValue(true), sheet.getCell(1, 1)?.computedValue)

        sheet.updateCell(1, 2, "=A1=10") // true
        assertEquals(CellValue.BooleanValue(true), sheet.getCell(1, 2)?.computedValue)
        
        sheet.updateCell(1, 3, "=A1<>10") // false
        assertEquals(CellValue.BooleanValue(false), sheet.getCell(1, 3)?.computedValue)
    }

    @Test
    fun `should evaluate IF function`() {
        sheet.updateCell(0, 0, "=IF(TRUE, 10, 20)")
        assertEquals(CellValue.DoubleValue(10.0), sheet.getCell(0, 0)?.computedValue)

        sheet.updateCell(0, 1, "=IF(FALSE, 10, 20)")
        assertEquals(CellValue.DoubleValue(20.0), sheet.getCell(0, 1)?.computedValue)
    }

    @Test
    fun `IF function should short-circuit`() {
        // The false branch contains a division by zero, but it should not be evaluated.
        sheet.updateCell(0, 0, "=IF(TRUE, 10, 1/0)")
        assertEquals(CellValue.DoubleValue(10.0), sheet.getCell(0, 0)?.computedValue)

        // The true branch contains an error, but it should not be evaluated.
        sheet.updateCell(0, 1, "=IF(FALSE, 1/0, 20)")
        assertEquals(CellValue.DoubleValue(20.0), sheet.getCell(0, 1)?.computedValue)
    }

    @Test
    fun `should evaluate AND and OR functions`() {
        sheet.updateCell(0, 0, "=AND(TRUE, TRUE, TRUE)")
        assertEquals(CellValue.BooleanValue(true), sheet.getCell(0, 0)?.computedValue)

        sheet.updateCell(0, 1, "=AND(TRUE, FALSE, TRUE)")
        assertEquals(CellValue.BooleanValue(false), sheet.getCell(0, 1)?.computedValue)

        sheet.updateCell(0, 2, "=OR(FALSE, FALSE, TRUE)")
        assertEquals(CellValue.BooleanValue(true), sheet.getCell(0, 2)?.computedValue)
        
        sheet.updateCell(0, 3, "=OR(FALSE, FALSE)")
        assertEquals(CellValue.BooleanValue(false), sheet.getCell(0, 3)?.computedValue)
    }

    @Test
    fun `should evaluate NOT function`() {
        sheet.updateCell(0, 0, "=NOT(TRUE)")
        assertEquals(CellValue.BooleanValue(false), sheet.getCell(0, 0)?.computedValue)
    }

    @Test
    fun `should evaluate LEN function`() {
        sheet.updateCell(0, 0, "hello")
        sheet.updateCell(0, 1, "=LEN(A1)")
        assertEquals(CellValue.DoubleValue(5.0), sheet.getCell(0, 1)?.computedValue)
    }

    @Test
    fun `should evaluate CONCATENATE function`() {
        sheet.updateCell(0, 0, "Hello")
        sheet.updateCell(0, 1, "World")
        sheet.updateCell(0, 2, "=CONCATENATE(A1, \" \", B1, \"!\")")
        assertEquals(CellValue.StringValue("Hello World!"), sheet.getCell(0, 2)?.computedValue)
    }

    @Test
    fun `should evaluate LEFT and RIGHT functions`() {
        sheet.updateCell(0, 0, "spreadsheet")
        sheet.updateCell(0, 1, "=LEFT(A1, 6)")
        assertEquals(CellValue.StringValue("spread"), sheet.getCell(0, 1)?.computedValue)

        sheet.updateCell(0, 2, "=RIGHT(A1, 5)")
        assertEquals(CellValue.StringValue("sheet"), sheet.getCell(0, 2)?.computedValue)
    }

    @Test
    fun `should evaluate VLOOKUP with exact match`() {
        // Setup table
        sheet.updateCell(0, 0, "100") // A1: ID
        sheet.updateCell(0, 1, "Apple") // B1: Item
        sheet.updateCell(0, 2, "1.20") // C1: Price
        sheet.updateCell(1, 0, "101")
        sheet.updateCell(1, 1, "Banana")
        sheet.updateCell(1, 2, "0.50")
        sheet.updateCell(2, 0, "102")
        sheet.updateCell(2, 1, "Cherry")
        sheet.updateCell(2, 2, "2.50")

        // Lookup "Banana" (ID 101) and get the price (3rd column)
        sheet.updateCell(0, 4, "=VLOOKUP(101, A1:C3, 3, FALSE)") // E1
        assertEquals(CellValue.DoubleValue(0.50), sheet.getCell(0, 4)?.computedValue)
    }

    @Test
    fun `VLOOKUP should return NA when not found`() {
        // Setup table
        sheet.updateCell(0, 0, "100")
        sheet.updateCell(0, 1, "Apple")
        sheet.updateCell(1, 0, "101")
        sheet.updateCell(1, 1, "Banana")

        // Lookup non-existent ID
        sheet.updateCell(0, 3, "=VLOOKUP(999, A1:B2, 2, FALSE)") // D1
        assertEquals(CellValue.ErrorValue("#N/A"), sheet.getCell(0, 3)?.computedValue)
    }

    @Test
    fun `VLOOKUP should return REF error for invalid column index`() {
        sheet.updateCell(0, 0, "100")
        sheet.updateCell(0, 1, "Apple")

        // col_index_num (4) is out of the table_array's (A1:B1) bounds
        sheet.updateCell(0, 3, "=VLOOKUP(100, A1:B1, 4, FALSE)")
        assertEquals(CellValue.ErrorValue("#REF!"), sheet.getCell(0, 3)?.computedValue)
    }

    @Test
    fun `should evaluate VLOOKUP with approximate match`() {
        // Setup sorted table
        sheet.updateCell(0, 0, "100")
        sheet.updateCell(0, 1, "Grade C")
        sheet.updateCell(1, 0, "200")
        sheet.updateCell(1, 1, "Grade B")
        sheet.updateCell(2, 0, "300")
        sheet.updateCell(2, 1, "Grade A")

        // Lookup 250. It should find the largest value <= 250, which is 200.
        sheet.updateCell(0, 3, "=VLOOKUP(250, A1:B3, 2, TRUE)")
        assertEquals(CellValue.StringValue("Grade B"), sheet.getCell(0, 3)?.computedValue)
        
        // Lookup 300. Exact match.
        sheet.updateCell(0, 4, "=VLOOKUP(300, A1:B3, 2)") // TRUE is default
        assertEquals(CellValue.StringValue("Grade A"), sheet.getCell(0, 4)?.computedValue)
    }

    @Test
    fun `should evaluate HLOOKUP with exact match`() {
        // Setup horizontal table
        sheet.updateCell(0, 0, "ID")      // A1
        sheet.updateCell(0, 1, "Item")    // B1
        sheet.updateCell(0, 2, "Price")   // C1
        sheet.updateCell(1, 0, "101")     // A2
        sheet.updateCell(1, 1, "Banana")  // B2
        sheet.updateCell(1, 2, "0.50")    // C2

        // Lookup "Item" in the first row (A1:C2) and get the value from the 2nd row in the same column
        sheet.updateCell(3, 0, "=HLOOKUP(\"Item\", A1:C2, 2, FALSE)")
        assertEquals(CellValue.StringValue("Banana"), sheet.getCell(3, 0)?.computedValue)
    }

    @Test
    fun `should add and retrieve named range`() {
        val range = CellRange(CellRef(0, 0), CellRef(1, 1)) // A1:B2
        sheet.addNamedRange("MyRange", range)
        assertEquals(range, sheet.getNamedRange("MyRange"))
    }

    @Test
    fun `should evaluate SUM function with a named range`() {
        sheet.updateCell(0, 0, "10") // A1
        sheet.updateCell(1, 0, "20") // A2
        sheet.updateCell(0, 1, "30") // B1
        sheet.updateCell(1, 1, "40") // B2

        val range = CellRange(CellRef(0, 0), CellRef(1, 1)) // A1:B2
        sheet.addNamedRange("MyRange", range)

        sheet.updateCell(2, 2, "=SUM(MyRange)") // C3
        assertEquals(CellValue.DoubleValue(100.0), sheet.getCell(2, 2)?.computedValue)
    }

    @Test
    fun `should return name error if named range not found`() {
        sheet.updateCell(0, 0, "=SUM(NonExistentRange)")
        assertEquals(CellValue.ErrorValue("#NAME?"), sheet.getCell(0, 0)?.computedValue)
    }
}
