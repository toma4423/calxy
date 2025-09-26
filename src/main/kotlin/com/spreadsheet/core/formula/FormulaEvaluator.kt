package com.spreadsheet.core.formula

import com.spreadsheet.core.spreadsheet.CellValue
import com.spreadsheet.core.spreadsheet.Sheet
import kotlin.math.max
import kotlin.math.min

class FormulaEvaluator {

    fun evaluate(sheet: Sheet, expression: Expression): CellValue {
        return when (expression) {
            is NumberLiteral -> CellValue.DoubleValue(expression.value)
            is BooleanLiteral -> CellValue.BooleanValue(expression.value)
            is StringLiteral -> CellValue.StringValue(expression.value)
            is CellRef -> evaluateCellRef(sheet, expression)
            is BinaryOperation -> evaluateBinaryOperation(sheet, expression)
            is FunctionCall -> evaluateFunctionCall(sheet, expression)
            is CellRange -> CellValue.ErrorValue("#VALUE!") // A range is not a value on its own
            is NamedRange -> CellValue.ErrorValue("#NAME?") // NamedRange cannot be evaluated directly to a CellValue
        }
    }

    private fun evaluateCellRef(sheet: Sheet, expression: CellRef): CellValue {
        return when (val cellValue = sheet.getCell(expression.row, expression.col)?.computedValue) {
            is CellValue.DoubleValue -> cellValue
            is CellValue.BooleanValue -> cellValue
            null -> CellValue.DoubleValue(0.0) // Treat empty as 0 for now
            else -> cellValue
        }
    }

    private fun evaluateBinaryOperation(sheet: Sheet, expression: BinaryOperation): CellValue {
        val left = evaluate(sheet, expression.left)
        val right = evaluate(sheet, expression.right)

        // Handle numeric operations
        if (left is CellValue.DoubleValue && right is CellValue.DoubleValue) {
            return when (expression.operator) {
                Operator.ADD -> CellValue.DoubleValue(left.value + right.value)
                Operator.SUBTRACT -> CellValue.DoubleValue(left.value - right.value)
                Operator.MULTIPLY -> CellValue.DoubleValue(left.value * right.value)
                Operator.DIVIDE -> {
                    if (right.value == 0.0) CellValue.ErrorValue("#DIV/0!")
                    else CellValue.DoubleValue(left.value / right.value)
                }
                Operator.GREATER_THAN -> CellValue.BooleanValue(left.value > right.value)
                Operator.LESS_THAN -> CellValue.BooleanValue(left.value < right.value)
                Operator.GREATER_OR_EQUAL -> CellValue.BooleanValue(left.value >= right.value)
                Operator.LESS_OR_EQUAL -> CellValue.BooleanValue(left.value <= right.value)
                Operator.EQUAL -> CellValue.BooleanValue(left.value == right.value)
                Operator.NOT_EQUAL -> CellValue.BooleanValue(left.value != right.value)
            }
        }
        
        // Handle string equality
        if (left is CellValue.StringValue && right is CellValue.StringValue) {
             return when (expression.operator) {
                Operator.EQUAL -> CellValue.BooleanValue(left.value == right.value)
                Operator.NOT_EQUAL -> CellValue.BooleanValue(left.value != right.value)
                else -> CellValue.ErrorValue("#VALUE!") // Other ops are not valid for strings
            }
        }

        return CellValue.ErrorValue("#VALUE!")
    }

    private fun evaluateFunctionCall(sheet: Sheet, expression: FunctionCall): CellValue {
        // Check for special forms first, which handle their own argument evaluation
        specialFunctions[expression.name]?.let {
            return it(this, sheet, expression.args)
        }

        // Standard function evaluation
        val function = builtinFunctions[expression.name]
            ?: return CellValue.ErrorValue("#NAME?") // Unknown function

        val evaluatedArgs = expression.args.flatMap { arg -> evaluateArgument(sheet, arg) }

        return function(evaluatedArgs)
    }

    private fun evaluateArgument(sheet: Sheet, expression: Expression): List<CellValue> {
        return when (expression) {
            is CellRange -> {
                val values = mutableListOf<CellValue>()
                val rowStart = min(expression.start.row, expression.end.row)
                val rowEnd = max(expression.start.row, expression.end.row)
                val colStart = min(expression.start.col, expression.end.col)
                val colEnd = max(expression.start.col, expression.end.col)

                for (r in rowStart..rowEnd) {
                    for (c in colStart..colEnd) {
                        values.add(evaluateCellRef(sheet, CellRef(c, r)))
                    }
                }
                values
            }
            is NamedRange -> {
                val namedRange = sheet.getNamedRange(expression.name)
                if (namedRange != null) {
                    evaluateArgument(sheet, namedRange) // Recursively evaluate the resolved CellRange
                } else {
                    listOf(CellValue.ErrorValue("#NAME?")) // Named range not found
                }
            }
            // For other types, evaluate them and return as a single-element list
            else -> listOf(evaluate(sheet, expression))
        }
    }
}
