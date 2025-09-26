package com.spreadsheet.core.spreadsheet

import com.spreadsheet.core.formula.Expression

// Represents the computed value of a cell.
sealed class CellValue {
    data class StringValue(val value: String) : CellValue()
    data class DoubleValue(val value: Double) : CellValue()
    data class BooleanValue(val value: Boolean) : CellValue()
    data class ErrorValue(val message: String) : CellValue() // For formula errors like #DIV/0!
    object Empty : CellValue()
}

/**
 * Represents a single cell in the spreadsheet.
 *
 * @param rawValue The raw string input by the user (e.g., "123", "Hello", "=A1+B1").
 * @param computedValue The calculated value after parsing and formula evaluation.
 * @param parsedExpression The parsed Abstract Syntax Tree of the formula, if any.
 */
data class Cell(
    val rawValue: String,
    val computedValue: CellValue,
    val parsedExpression: Expression? = null
)
