# Built-in Functions: Text

This document specifies the behavior of standard text manipulation functions.

---

### CONCATENATE

- **Description:** Joins several text items into one text item.
- **Syntax:** `CONCATENATE(text1, [text2], ...)`
- **Logic:** Converts all arguments to their string representation and joins them together in order.

### LEFT

- **Description:** Returns the first character or characters in a text string.
- **Syntax:** `LEFT(text, [num_chars])`
- **Logic:** 
    - Returns the first `num_chars` characters from `text`.
    - If `num_chars` is omitted, it defaults to 1.
    - If `num_chars` is negative, a `#VALUE!` error is returned.

### RIGHT

- **Description:** Returns the last character or characters in a text string.
- **Syntax:** `RIGHT(text, [num_chars])`
- **Logic:** 
    - Returns the last `num_chars` characters from `text`.
    - If `num_chars` is omitted, it defaults to 1.
    - If `num_chars` is negative, a `#VALUE!` error is returned.

### LEN

- **Description:** Returns the number of characters in a text string.
- **Syntax:** `LEN(text)`
- **Logic:** Takes one argument, converts it to its string representation, and returns its length as a number.
