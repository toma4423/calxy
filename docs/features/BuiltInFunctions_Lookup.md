# Built-in Functions: Lookup & Reference

This document specifies the behavior of lookup and reference functions.

---

### VLOOKUP

- **Description:** Looks for a value in the leftmost column of a table, and then returns a value in the same row from a column you specify. 
- **Syntax:** `VLOOKUP(lookup_value, table_array, col_index_num, [range_lookup])`
- **Arguments:**
    - `lookup_value`: The value to search for.
    - `table_array`: The range of cells that contains the data.
    - `col_index_num`: The column number in `table_array` from which the matching value should be returned. The first column is 1.
    - `range_lookup`: (Optional) A boolean value. 
        - `TRUE` or omitted: Approximate match. Finds the largest value that is less than or equal to `lookup_value` in the first column. The first column must be sorted in ascending order.
        - `FALSE`: Exact match. If an exact match is not found, an `#N/A` error is returned.
- **Implementation Notes:**
    - Supports both exact and approximate matches.
    - If `col_index_num` is less than 1 or greater than the number of columns in `table_array`, a `#REF!` error is returned.

---

### HLOOKUP

- **Description:** Looks for a value in the top row of a table, and then returns a value in the same column from a row you specify.
- **Syntax:** `HLOOKUP(lookup_value, table_array, row_index_num, [range_lookup])`
- **Arguments:**
    - `lookup_value`: The value to search for.
    - `table_array`: The range of cells that contains the data.
    - `row_index_num`: The row number in `table_array` from which the matching value should be returned. The first row is 1.
    - `range_lookup`: (Optional) A boolean value. 
        - `TRUE` or omitted: Approximate match. Finds the largest value that is less than or equal to `lookup_value` in the top row. The top row must be sorted in ascending order.
        - `FALSE`: Exact match. If an exact match is not found, an `#N/A` error is returned.
- **Implementation Notes:**
    - Supports both exact and approximate matches.
    - If `row_index_num` is less than 1 or greater than the number of rows in `table_array`, a `#REF!` error is returned.