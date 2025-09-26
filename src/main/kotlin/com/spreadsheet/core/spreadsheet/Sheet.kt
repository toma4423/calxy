package com.spreadsheet.core.spreadsheet

import com.spreadsheet.core.formula.*

typealias CellPosition = Pair<Int, Int>

class Sheet(
    private val parser: FormulaParser = FormulaParser(),
    private val evaluator: FormulaEvaluator = FormulaEvaluator()
) {
    private val cells: MutableMap<CellPosition, Cell> = mutableMapOf()

    // A -> {B, C} means B and C depend on A.
    private val dependents: MutableMap<CellPosition, MutableSet<CellPosition>> = mutableMapOf()
    // B -> {A} means B depends on A.
        private val precedents: MutableMap<CellPosition, MutableSet<CellPosition>> = mutableMapOf()
    
        private val namedRanges: MutableMap<String, CellRange> = mutableMapOf()
    
    
        fun getCell(row: Int, col: Int): Cell? {
            return cells[row to col]
        }
        
        fun addNamedRange(name: String, range: CellRange) {
            namedRanges[name.uppercase()] = range
        }
    
        fun getNamedRange(name: String): CellRange? {
            return namedRanges[name.uppercase()]
        }
        
    // Public getters for testing dependency graph
    fun getDependents(row: Int, col: Int): Set<CellPosition>? = dependents[row to col]
    fun getPrecedents(row: Int, col: Int): Set<CellPosition>? = precedents[row to col]

    fun updateCell(row: Int, col: Int, rawValue: String) {
        val position = row to col
        val oldPrecedents = precedents[position]?.toSet() ?: emptySet()

        val parsedExpression: Expression?
        try {
            parsedExpression = parser.parse(rawValue)
        } catch (e: Exception) {
            // On syntax error, clear dependencies and set error
            clearDependencies(position, oldPrecedents)
            cells[position] = Cell(rawValue, CellValue.ErrorValue("#ERROR!"), null)
            recalculate(position)
            return
        }

        val newPrecedents = if (parsedExpression != null) extractPrecedents(parsedExpression) else emptySet()

        // Check for circular references
        if (detectsCircularReference(position, newPrecedents)) {
            cells[position] = Cell(rawValue, CellValue.ErrorValue("#REF!"), parsedExpression)
            // Do not update dependencies, just recalculate old dependents if the value changed
            // This logic is complex, for now we just set error. A full implementation
            // might revert to the old formula or clear dependencies.
            // For now, we leave the old dependencies in place and just set the error.
            return
        }

        // No cycle, so commit dependency changes
        // 1. Remove this cell from its old precedents' dependent lists
        oldPrecedents.forEach { precedentPos ->
            dependents[precedentPos]?.let { dependentSet ->
                dependentSet.remove(position)
                if (dependentSet.isEmpty()) {
                    dependents.remove(precedentPos)
                }
            }
        }

        // 2. Set new precedents for this cell
        if (newPrecedents.isNotEmpty()) {
            precedents[position] = newPrecedents.toMutableSet()
        } else {
            precedents.remove(position)
        }

        // 3. Add this cell to its new precedents' dependent lists
        newPrecedents.forEach { precedentPos ->
            dependents.getOrPut(precedentPos) { mutableSetOf() }.add(position)
        }

        // Evaluate the formula
        val computedValue = if (parsedExpression != null) {
            evaluator.evaluate(this, parsedExpression)
        } else {
            rawValue.toDoubleOrNull()?.let { CellValue.DoubleValue(it) }
                ?: CellValue.StringValue(rawValue)
        }

        cells[position] = Cell(rawValue, computedValue, parsedExpression)

        // Trigger recalculation of dependent cells
        recalculate(position)
    }

    private fun clearDependencies(position: CellPosition, precedentsToClear: Set<CellPosition>) {
        precedentsToClear.forEach { precedentPos ->
            dependents[precedentPos]?.let { dependentSet ->
                dependentSet.remove(position)
                if (dependentSet.isEmpty()) {
                    dependents.remove(precedentPos)
                }
            }
        }
        precedents.remove(position)
    }

    private fun detectsCircularReference(cellPos: CellPosition, newPrecedents: Set<CellPosition>): Boolean {
        // Direct cycle: A1 = A1
        if (cellPos in newPrecedents) return true
        // Indirect cycle: A1 = B1, B1 = A1
        for (precedent in newPrecedents) {
            if (isPrecedent(precedent, cellPos)) {
                return true
            }
        }
        return false
    }

    // Is `target` a precedent of `start` (or a precedent of a precedent, etc.)?
    private fun isPrecedent(start: CellPosition, target: CellPosition): Boolean {
        val toVisit = ArrayDeque<CellPosition>()
        precedents[start]?.let { toVisit.addAll(it) }
        val visited = mutableSetOf<CellPosition>()

        while (toVisit.isNotEmpty()) {
            val current = toVisit.removeFirst()
            if (current == target) return true
            if (current !in visited) {
                visited.add(current)
                precedents[current]?.let { toVisit.addAll(it) }
            }
        }
        return false
    }

    private fun recalculate(position: CellPosition) {
        // Using BFS to traverse dependents and recalculate
        val queue = ArrayDeque<CellPosition>()
        dependents[position]?.let { queue.addAll(it) }
        val visited = mutableSetOf<CellPosition>()

        while(queue.isNotEmpty()){
            val currentPos = queue.removeFirst()
            if(currentPos in visited) continue
            visited.add(currentPos)

            val cell = cells[currentPos]
            val expression = cell?.parsedExpression
            if (cell != null && expression != null) {
                val newValue = evaluator.evaluate(this, expression)
                if (newValue != cell.computedValue) {
                    cells[currentPos] = cell.copy(computedValue = newValue)
                    // Add children of the updated cell to the queue
                    dependents[currentPos]?.let { queue.addAll(it) }
                }
            }
        }
    }

    private fun extractPrecedents(expression: Expression): Set<CellPosition> {
        val refs = mutableSetOf<CellPosition>()
        fun traverse(expr: Expression) {
            when (expr) {
                is CellRef -> refs.add(expr.row to expr.col)
                is BinaryOperation -> {
                    traverse(expr.left)
                    traverse(expr.right)
                }
                is NumberLiteral -> { /* No-op */ }
                is BooleanLiteral -> { /* No-op */ }
                is StringLiteral -> { /* No-op */ }
                is NamedRange -> { /* No-op */ }
                is FunctionCall -> expr.args.forEach { traverse(it) }
                is CellRange -> {
                    traverse(expr.start)
                    traverse(expr.end)
                }
            }
        }
        traverse(expression)
        return refs
    }
}
