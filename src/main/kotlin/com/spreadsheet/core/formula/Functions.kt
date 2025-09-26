package com.spreadsheet.core.formula

import com.spreadsheet.core.spreadsheet.CellValue
import com.spreadsheet.core.spreadsheet.Sheet
import kotlin.math.max
import kotlin.math.min

// Standard functions that receive evaluated arguments
typealias BuiltinFunction = (List<CellValue>) -> CellValue

// Special forms that receive the evaluator and unevaluated argument expressions
typealias SpecialForm = (FormulaEvaluator, Sheet, List<Expression>) -> CellValue

val builtinFunctions: Map<String, BuiltinFunction> = mapOf(
    "SUM" to ::sum,
    "AVERAGE" to ::average,
    "COUNT" to ::count,
    "MAX" to ::max,
    "MIN" to ::min,
    "NOT" to ::not,
    // Text functions
    "CONCATENATE" to ::concatenate,
    "LEFT" to ::left,
    "RIGHT" to ::right,
    "LEN" to ::len
)

val specialFunctions: Map<String, SpecialForm> = mapOf(
    "IF" to ::if_,
    "AND" to ::and_,
    "OR" to ::or_,
    "VLOOKUP" to ::vlookup,
    "HLOOKUP" to ::hlookup
)

// --- Helper --- 
private fun CellValue.asBoolean(): Boolean? {
    return when (this) {
        is CellValue.BooleanValue -> this.value
        is CellValue.DoubleValue -> this.value != 0.0
        else -> null
    }
}

private fun CellValue.asString(): String {
    return when (this) {
        is CellValue.StringValue -> this.value
        is CellValue.DoubleValue -> this.value.toString()
        is CellValue.BooleanValue -> this.value.toString().uppercase()
        is CellValue.ErrorValue -> this.message
        is CellValue.Empty -> ""
    }
}

private fun propagateError(args: List<CellValue>): CellValue? {
    return args.firstOrNull { it is CellValue.ErrorValue }
}

// --- Special Forms ---

private fun if_(evaluator: FormulaEvaluator, sheet: Sheet, args: List<Expression>): CellValue {
    if (args.size !in 2..3) return CellValue.ErrorValue("#N/A")

    val condition = evaluator.evaluate(sheet, args[0]).asBoolean() 
        ?: return CellValue.ErrorValue("#VALUE!")

    return if (condition) {
        evaluator.evaluate(sheet, args[1]) // value_if_true
    } else {
        if (args.size == 3) evaluator.evaluate(sheet, args[2]) else CellValue.BooleanValue(false)
    }
}

private fun and_(evaluator: FormulaEvaluator, sheet: Sheet, args: List<Expression>): CellValue {
    if (args.isEmpty()) return CellValue.ErrorValue("#VALUE!")
    for (arg in args) {
        val result = evaluator.evaluate(sheet, arg).asBoolean() 
            ?: return CellValue.ErrorValue("#VALUE!")
        if (!result) return CellValue.BooleanValue(false) // Short-circuit
    }
    return CellValue.BooleanValue(true)
}

private fun or_(evaluator: FormulaEvaluator, sheet: Sheet, args: List<Expression>): CellValue {
    if (args.isEmpty()) return CellValue.ErrorValue("#VALUE!")
    for (arg in args) {
        val result = evaluator.evaluate(sheet, arg).asBoolean() 
            ?: return CellValue.ErrorValue("#VALUE!")
        if (result) return CellValue.BooleanValue(true) // Short-circuit
    }
    return CellValue.BooleanValue(false)
}

// --- Standard Functions ---

private fun not(args: List<CellValue>): CellValue {
    if (args.size != 1) return CellValue.ErrorValue("#N/A")
    val bool = args.first().asBoolean() ?: return CellValue.ErrorValue("#VALUE!")
    return CellValue.BooleanValue(!bool)
}

private fun sum(args: List<CellValue>): CellValue {
    propagateError(args)?.let { return it }
    val sum = args.sumOf { 
        (it as? CellValue.DoubleValue)?.value ?: 0.0 
    }
    return CellValue.DoubleValue(sum)
}

private fun average(args: List<CellValue>): CellValue {
    propagateError(args)?.let { return it }
    val numbers = args.filterIsInstance<CellValue.DoubleValue>()
    return if (numbers.isNotEmpty()) {
        CellValue.DoubleValue(numbers.sumOf { it.value } / numbers.size)
    } else {
        CellValue.ErrorValue("#DIV/0!")
    }
}

private fun count(args: List<CellValue>): CellValue {
    propagateError(args)?.let { return it }
    val count = args.count { it is CellValue.DoubleValue }
    return CellValue.DoubleValue(count.toDouble())
}

private fun max(args: List<CellValue>): CellValue {
    propagateError(args)?.let { return it }
    val numbers = args.mapNotNull { (it as? CellValue.DoubleValue)?.value }
    return if (numbers.isNotEmpty()) CellValue.DoubleValue(numbers.maxOrNull()!!)
    else CellValue.ErrorValue("#VALUE!") // Excel returns #VALUE! if no numbers
}

private fun min(args: List<CellValue>): CellValue {
    propagateError(args)?.let { return it }
    val numbers = args.mapNotNull { (it as? CellValue.DoubleValue)?.value }
    return if (numbers.isNotEmpty()) CellValue.DoubleValue(numbers.minOrNull()!!)
    else CellValue.ErrorValue("#VALUE!") // Excel returns #VALUE! if no numbers
}

// --- Text Functions ---

private fun concatenate(args: List<CellValue>): CellValue {
    propagateError(args)?.let { return it }
    return CellValue.StringValue(args.joinToString("") { it.asString() })
}

private fun left(args: List<CellValue>): CellValue {
    propagateError(args)?.let { return it }
    if (args.size !in 1..2) return CellValue.ErrorValue("#N/A")
    val text = args[0].asString()
    val numChars = if (args.size == 2) {
        (args[1] as? CellValue.DoubleValue)?.value?.toInt() ?: return CellValue.ErrorValue("#VALUE!")
    } else {
        1
    }

    if (numChars < 0) return CellValue.ErrorValue("#VALUE!")
    return CellValue.StringValue(text.take(numChars))
}

private fun right(args: List<CellValue>): CellValue {
    propagateError(args)?.let { return it }
    if (args.size !in 1..2) return CellValue.ErrorValue("#N/A")
    val text = args[0].asString()
    val numChars = if (args.size == 2) {
        (args[1] as? CellValue.DoubleValue)?.value?.toInt() ?: return CellValue.ErrorValue("#VALUE!")
    } else {
        1
    }

    if (numChars < 0) return CellValue.ErrorValue("#VALUE!")
    return CellValue.StringValue(text.takeLast(numChars))
}

private fun len(args: List<CellValue>): CellValue {
    propagateError(args)?.let { return it }
    if (args.size != 1) return CellValue.ErrorValue("#N/A")
    return CellValue.DoubleValue(args[0].asString().length.toDouble())
}

// --- Lookup Functions ---

private fun vlookup(evaluator: FormulaEvaluator, sheet: Sheet, args: List<Expression>): CellValue {
    // VLOOKUP handles its own error propagation for lookupValue and colIndex
    if (args.size !in 3..4) return CellValue.ErrorValue("#N/A")

    val lookupValue = evaluator.evaluate(sheet, args[0])
    if (lookupValue is CellValue.ErrorValue) return lookupValue

    val tableArrayExpr = args[1]
    val colIndexExpr = args[2]
    val rangeLookupExpr = if (args.size == 4) args[3] else null

    if (tableArrayExpr !is CellRange) return CellValue.ErrorValue("#REF!")

    val colIndex = (evaluator.evaluate(sheet, colIndexExpr) as? CellValue.DoubleValue)?.value?.toInt()
        ?: return CellValue.ErrorValue("#VALUE!")

    if (colIndex < 1) return CellValue.ErrorValue("#VALUE!")

    val rangeLookup = rangeLookupExpr?.let { 
        val result = evaluator.evaluate(sheet, it)
        if (result is CellValue.ErrorValue) return result
        result.asBoolean() ?: return CellValue.ErrorValue("#VALUE!")
    } ?: true // Default to TRUE (approximate match)

    val rowStart = min(tableArrayExpr.start.row, tableArrayExpr.end.row)
    val rowEnd = max(tableArrayExpr.start.row, tableArrayExpr.end.row)
    val colStart = min(tableArrayExpr.start.col, tableArrayExpr.end.col)
    val colEnd = max(tableArrayExpr.start.col, tableArrayExpr.end.col)

    if (colIndex > (colEnd - colStart + 1)) return CellValue.ErrorValue("#REF!")

    if (!rangeLookup) { // Exact match
        for (r in rowStart..rowEnd) {
            val valueToMatch = evaluator.evaluate(sheet, CellRef(colStart, r))
            if (valueToMatch == lookupValue) {
                val resultCol = colStart + colIndex - 1
                return evaluator.evaluate(sheet, CellRef(resultCol, r))
            }
        }
    } else { // Approximate match
        var bestMatchRow = -1
        // This assumes the first column is sorted in ascending order.
        for (r in rowStart..rowEnd) {
            val valueToMatch = evaluator.evaluate(sheet, CellRef(colStart, r))
            if (valueToMatch is CellValue.DoubleValue && lookupValue is CellValue.DoubleValue) {
                if (valueToMatch.value <= lookupValue.value) {
                    bestMatchRow = r
                } else {
                    break // Stop since the list is sorted
                }
            }
            // TODO: Add support for text-based approximate match
        }
        if (bestMatchRow != -1) {
            val resultCol = colStart + colIndex - 1
            return evaluator.evaluate(sheet, CellRef(resultCol, bestMatchRow))
        }
    }

    return CellValue.ErrorValue("#N/A") // Not found
}

private fun hlookup(evaluator: FormulaEvaluator, sheet: Sheet, args: List<Expression>): CellValue {
    // HLOOKUP handles its own error propagation for lookupValue and rowIndex
    if (args.size !in 3..4) return CellValue.ErrorValue("#N/A")

    val lookupValue = evaluator.evaluate(sheet, args[0])
    if (lookupValue is CellValue.ErrorValue) return lookupValue

    val tableArrayExpr = args[1]
    val rowIndexExpr = args[2]
    val rangeLookupExpr = if (args.size == 4) args[3] else null

    if (tableArrayExpr !is CellRange) return CellValue.ErrorValue("#REF!")

    val rowIndex = (evaluator.evaluate(sheet, rowIndexExpr) as? CellValue.DoubleValue)?.value?.toInt()
        ?: return CellValue.ErrorValue("#VALUE!")
    if (rowIndex < 1) return CellValue.ErrorValue("#VALUE!")

    val rangeLookup = rangeLookupExpr?.let { 
        val result = evaluator.evaluate(sheet, it)
        if (result is CellValue.ErrorValue) return result
        result.asBoolean() ?: return CellValue.ErrorValue("#VALUE!")
    } ?: true

    val rowStart = min(tableArrayExpr.start.row, tableArrayExpr.end.row)
    val rowEnd = max(tableArrayExpr.start.row, tableArrayExpr.end.row)
    val colStart = min(tableArrayExpr.start.col, tableArrayExpr.end.col)
    val colEnd = max(tableArrayExpr.start.col, tableArrayExpr.end.col)

    if (rowIndex > (rowEnd - rowStart + 1)) return CellValue.ErrorValue("#REF!")

    if (!rangeLookup) { // Exact match
        for (c in colStart..colEnd) {
            val valueToMatch = evaluator.evaluate(sheet, CellRef(c, rowStart))
            if (valueToMatch == lookupValue) {
                val resultRow = rowStart + rowIndex - 1
                return evaluator.evaluate(sheet, CellRef(c, resultRow))
            }
        }
    } else { // Approximate match
        var bestMatchCol = -1
        for (c in colStart..colEnd) {
            val valueToMatch = evaluator.evaluate(sheet, CellRef(c, rowStart))
            if (valueToMatch is CellValue.DoubleValue && lookupValue is CellValue.DoubleValue) {
                if (valueToMatch.value <= lookupValue.value) {
                    bestMatchCol = c
                } else {
                    break
                }
            }
        }
        if (bestMatchCol != -1) {
            val resultRow = rowStart + rowIndex - 1
            return evaluator.evaluate(sheet, CellRef(bestMatchCol, resultRow))
        }
    }

    return CellValue.ErrorValue("#N/A") // Not found
}
