# Built-in Functions: Aggregate

This document specifies the behavior of standard aggregate functions.

## Common Behavior

- All aggregate functions operate on a list of values, which can be provided as individual arguments or as a cell range (e.g., `A1:B10`).
- When processing arguments, these functions only consider cells containing numeric (`DoubleValue`) values.
- Text values, error values, and empty cells are ignored in the calculation (except for `COUNT`, which has specific rules).

---

### SUM

- **Description:** Adds all the numbers in a list of arguments.
- **Syntax:** `SUM(number1, [number2], ...)`
- **Logic:** Returns the sum of all `DoubleValue` arguments. Non-numeric values are treated as 0.

### AVERAGE

- **Description:** Returns the arithmetic mean of its arguments.
- **Syntax:** `AVERAGE(number1, [number2], ...)`
- **Logic:** Calculates the sum of all `DoubleValue` arguments and divides by the count of those numeric arguments. If no numeric arguments are provided, it returns a `#DIV/0!` error.

### COUNT

- **Description:** Counts the number of cells that contain numbers.
- **Syntax:** `COUNT(value1, [value2], ...)`
- **Logic:** Returns the total count of `DoubleValue` arguments.

### MAX

- **Description:** Returns the largest value in a set of values.
- **Syntax:** `MAX(number1, [number2], ...)`
- **Logic:** Returns the largest numeric value among the arguments. If no numeric values are found, it returns `0.0`.

### MIN

- **Description:** Returns the smallest value in a set of values.
- **Syntax:** `MIN(number1, [number2], ...)`
- **Logic:** Returns the smallest numeric value among the arguments. If no numeric values are found, it returns `0.0`.
