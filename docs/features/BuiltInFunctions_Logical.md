# Built-in Functions: Logical

This document specifies the behavior of standard logical functions.

## Common Behavior

- Logical functions treat numeric value `0.0` as `FALSE` and any other number as `TRUE`.
- Text values or error values in a condition will result in a `#VALUE!` error.

---

### IF

- **Description:** Returns one value if a condition is true and another value if it's false.
- **Syntax:** `IF(logical_test, value_if_true, [value_if_false])`
- **Logic:**
    - Evaluates `logical_test`. 
    - If the result is `TRUE`, it evaluates and returns `value_if_true`.
    - If the result is `FALSE`, it evaluates and returns `value_if_false`.
    - If `value_if_false` is omitted and the condition is false, it returns `FALSE`.
    - This function uses short-circuiting; only one of the result branches is ever evaluated.

### AND

- **Description:** Returns `TRUE` if all of its arguments are `TRUE`.
- **Syntax:** `AND(logical1, [logical2], ...)`
- **Logic:**
    - Evaluates arguments from left to right.
    - If any argument evaluates to `FALSE`, it immediately returns `FALSE` without evaluating the rest (short-circuiting).
    - If all arguments evaluate to `TRUE`, it returns `TRUE`.

### OR

- **Description:** Returns `TRUE` if any of its arguments are `TRUE`.
- **Syntax:** `OR(logical1, [logical2], ...)`
- **Logic:**
    - Evaluates arguments from left to right.
    - If any argument evaluates to `TRUE`, it immediately returns `TRUE` without evaluating the rest (short-circuiting).
    - If all arguments evaluate to `FALSE`, it returns `FALSE`.

### NOT

- **Description:** Reverses the logic of its argument.
- **Syntax:** `NOT(logical)`
- **Logic:**
    - If the argument is `FALSE`, returns `TRUE`.
    - If the argument is `TRUE`, returns `FALSE`.
