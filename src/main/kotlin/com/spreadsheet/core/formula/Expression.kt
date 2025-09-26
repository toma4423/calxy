package com.spreadsheet.core.formula

/**
 * Represents a node in the Abstract Syntax Tree (AST) of a parsed formula.
 */
sealed class Expression

data class NumberLiteral(val value: Double) : Expression()
data class CellRef(val col: Int, val row: Int) : Expression()

data class CellRange(val start: CellRef, val end: CellRef) : Expression()

data class FunctionCall(val name: String, val args: List<Expression>) : Expression()

data class BooleanLiteral(val value: Boolean) : Expression()

data class StringLiteral(val value: String) : Expression()

data class NamedRange(val name: String) : Expression()

data class BinaryOperation(
    val left: Expression,
    val operator: Operator,
    val right: Expression
) : Expression()

enum class Operator {
    ADD, SUBTRACT, MULTIPLY, DIVIDE,
    GREATER_THAN, LESS_THAN, GREATER_OR_EQUAL, LESS_OR_EQUAL, EQUAL, NOT_EQUAL
}
