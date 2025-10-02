# TODO / Next Steps

This document outlines the next development tasks based on the project specification.

## Phase 1: MVP Core Implementation (Completed)

The core engine and basic UI structure for the MVP are now complete.

### 1. Core Model & Calculation Engine
- **Files:** `core/spreadsheet/`, `core/formula/`
- **Tasks:**
    - [x] Define data classes for `Sheet` and `Cell`, including `CellValue` sealed class.
    - [x] Implement the grid model using a sparse map in `Sheet.kt`.
    - [x] Implement a Parser/Evaluator architecture for the formula engine.
    - [x] Implement parsing for cell references, operator precedence, and parentheses.
    - [x] Implement dependency graph for tracking cell relationships.
    - [x] Implement circular reference detection (`#REF!`).
    - [x] Implement automatic recalculation engine.

### 2. Basic UI Implementation
- **Files:** `Main.kt`
- **Tasks:**
    - [x] Create the main application window.
    - [x] Implement a basic, scrollable grid of cells using Jetpack Compose.
    - [x] Display computed cell values from the `Sheet` model in the UI grid.

### 3. Built-in Functions (Phase 1)
- **Files:** `core/formula/Functions.kt`
- **Tasks:**
    - [x] Implement basic aggregate functions: `SUM`, `AVERAGE`, `COUNT`, `MAX`, `MIN`.
    - [x] Implement cell range (`A1:B10`) parsing and evaluation.

### 4. Core Technology Verification
- **Tasks:**
    - [x] **Python Integration:** Verified that Kotlin can execute Python code and exchange data via GraalVM.
    - [x] **File I/O:** Verified that basic `.xlsx` files can be read and written using Apache POI.
    - [x] **Database:** Verified that a connection can be made and data can be manipulated using the Exposed ORM framework.

---

## Phase 2: Feature Expansion

### 1. Formula Engine
- [x] Implement logical functions (`IF`, `AND`, `OR`, `NOT`).
- [x] Implement text functions (`CONCATENATE`, `LEFT`, `RIGHT`, `LEN`).
- [x] Implement lookup functions (`VLOOKUP`, `HLOOKUP`).
- [ ] Add support for named ranges.

### 2. UI/UX
- [ ] Implement cell selection and a formula input bar.
- [ ] Implement cell editing (typing directly into cells).
- [ ] Implement basic cell formatting (bold, italics, colors).

### 3. File I/O
- [ ] Implement `ExcelReader.kt` to fully load `.xlsx` files into the `Sheet` model.
- [ ] Implement `ExcelWriter.kt` to save the `Sheet` model to an `.xlsx` file.

### 4. Other
- [x] **GitHub Release:** Build the Linux package (`.deb`). (Uploading to GitHub Release is a separate step.)

### 5. CI/CD & Release Automation
- [x] Implement GitHub Actions for CI (build and test).
- [x] Implement GitHub Actions for multi-platform release (Linux, macOS, Windows).
- [ ] Verify successful execution of CI workflow on GitHub Actions.
- [ ] Verify successful execution of multi-platform release workflow on GitHub Actions.
- [ ] **GitHub Release:** Upload the Linux package (`.deb`) to a GitHub Release.
- [ ] **GitHub Release:** Upload the macOS package (`.dmg`) to a GitHub Release.
- [ ] **GitHub Release:** Upload the Windows package (`.msi`) to a GitHub Release.