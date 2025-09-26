package com.spreadsheet.core.formula

import java.util.regex.Pattern

class FormulaParser {

    private var text: String = ""
    private var pos: Int = -1
    private var currentChar: Char? = null

    private val cellRefPattern: Pattern = Pattern.compile("([A-Z]+)([1-9][0-9]*)")

    private fun advance() {
        pos++
        currentChar = if (pos < text.length) text[pos] else null
    }

    private fun peek(): Char? {
        val peekPos = pos + 1
        return if (peekPos < text.length) text[peekPos] else null
    }

    private fun skipWhitespace() {
        while (currentChar != null && currentChar!!.isWhitespace()) {
            advance()
        }
    }

    fun parse(rawValue: String): Expression? {
        if (!rawValue.startsWith("=")) return null
        text = rawValue.substring(1)
        pos = -1
        advance()
        try {
            val expr = parseExpression()
            skipWhitespace()
            if (currentChar != null) throw IllegalArgumentException("Unexpected character at end of formula: $currentChar")
            return expr
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid formula: $rawValue", e)
        }
    }

    // Entry point for parsing, lowest precedence
    private fun parseExpression(): Expression = parseComparison()

    // Handles >, <, =, etc.
    private fun parseComparison(): Expression {
        var expr = parseAdditionSubtraction()
        skipWhitespace()
        while (currentChar in listOf('<', '>', '=')) {
            val op = when (currentChar) {
                '=' -> Operator.EQUAL
                '<' -> {
                    advance()
                    when (currentChar) {
                        '>' -> Operator.NOT_EQUAL
                        '=' -> Operator.LESS_OR_EQUAL
                        else -> {
                            pos-- // Backtrack
                            currentChar = '<'
                            Operator.LESS_THAN
                        }
                    }
                }
                '>' -> {
                    advance()
                    if (currentChar == '=') Operator.GREATER_OR_EQUAL else {
                        pos-- // Backtrack
                        currentChar = '>'
                        Operator.GREATER_THAN
                    }
                }
                else -> throw IllegalStateException()
            }
            advance()
            val right = parseAdditionSubtraction()
            expr = BinaryOperation(expr, op, right)
            skipWhitespace()
        }
        return expr
    }

    // Handles + and -
    private fun parseAdditionSubtraction(): Expression {
        var expr = parseMultiplicationDivision()
        skipWhitespace()
        while (currentChar in listOf('+', '-')) {
            val op = when (currentChar) {
                '+' -> Operator.ADD
                '-' -> Operator.SUBTRACT
                else -> throw IllegalStateException()
            }
            advance()
            val right = parseMultiplicationDivision()
            expr = BinaryOperation(expr, op, right)
            skipWhitespace()
        }
        return expr
    }

    // Handles * and /
    private fun parseMultiplicationDivision(): Expression {
        var expr = parseFactor()
        skipWhitespace()
        while (currentChar in listOf('*', '/')) {
            val op = when (currentChar) {
                '*' -> Operator.MULTIPLY
                '/' -> Operator.DIVIDE
                else -> throw IllegalStateException()
            }
            advance()
            val right = parseFactor()
            expr = BinaryOperation(expr, op, right)
            skipWhitespace()
        }
        return expr
    }

    private fun parseFactor(): Expression {
        skipWhitespace()
        if (currentChar == '"') {
            advance() // Consume opening '"'
            val sb = StringBuilder()
            while (currentChar != null && currentChar != '"') {
                sb.append(currentChar)
                advance()
            }
            if (currentChar != '"') {
                throw IllegalArgumentException("Unterminated string literal")
            }
            advance() // Consume closing '"'
            return StringLiteral(sb.toString())
        }
        if (currentChar == '(') {
            advance()
            val expr = parseExpression()
            skipWhitespace()
            if (currentChar != ')') throw IllegalArgumentException("Mismatched parentheses: expected ')'")
            advance()
            return expr
        }
        if (currentChar?.isDigit() == true || currentChar == '.') {
            val sb = StringBuilder()
            while (currentChar?.isDigit() == true || currentChar == '.') {
                sb.append(currentChar)
                advance()
            }
            return NumberLiteral(sb.toString().toDouble())
        }
        if (currentChar?.isLetter() == true) {
            return parseIdentifier()
        }
        throw IllegalArgumentException("Invalid syntax at position $pos")
    }

    private fun parseIdentifier(): Expression {
        val startPos = pos
        while (currentChar?.isLetterOrDigit() == true) {
            advance()
        }
        val name = text.substring(startPos, pos).uppercase()
        skipWhitespace()
        if (currentChar == '(') {
            return parseFunctionCall(name)
        }
        return when (name) {
            "TRUE" -> BooleanLiteral(true)
            "FALSE" -> BooleanLiteral(false)
            else -> {
                // Try to parse as a CellRef first
                val matcher = cellRefPattern.matcher(name)
                if (matcher.matches()) {
                    val colStr = matcher.group(1)
                    val rowStr = matcher.group(2)
                    val col = columnToIndex(colStr)
                    val row = rowStr.toInt() - 1
                    return CellRef(col, row)
                }
                // If not a CellRef, then it must be a NamedRange
                NamedRange(name)
            }
        }
    }

    private fun parseFunctionCall(name: String): Expression {
        advance() // Consume '('
        val args = mutableListOf<Expression>()
        skipWhitespace()
        if (currentChar != ')') {
            while (true) {
                args.add(parseRangeExpression())
                skipWhitespace()
                if (currentChar == ')') break
                if (currentChar != ',') throw IllegalArgumentException("Expected ',' or ')' in function arguments")
                advance() // Consume ','
            }
        }
        advance() // Consume ')'
        return FunctionCall(name, args)
    }

    private fun parseRangeExpression(): Expression {
        val start = parseExpression()
        skipWhitespace()
        if (currentChar == ':') {
            advance() // Consume ':'
            val end = parseExpression()
            if (start is CellRef && end is CellRef) {
                return CellRange(start, end)
            }
            throw IllegalArgumentException("Invalid range expression")
        }
        return start
    }

    private fun parseCellReference(name: String): CellRef {
        val matcher = cellRefPattern.matcher(name)
        if (matcher.matches()) {
            val colStr = matcher.group(1)
            val rowStr = matcher.group(2)
            val col = columnToIndex(colStr)
            val row = rowStr.toInt() - 1
            return CellRef(col, row)
        }
        throw IllegalArgumentException("Invalid cell reference: $name")
    }

    private fun columnToIndex(column: String): Int {
        var result = 0
        for (char in column) {
            result = result * 26 + (char - 'A' + 1)
        }
        return result - 1
    }
}