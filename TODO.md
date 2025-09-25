# TODO / Next Steps

This document outlines the next development tasks based on the project specification.

## Phase 1: MVP Development

The primary goal is to complete the Minimum Viable Product (MVP) as a standalone desktop application.

### 1. Core Model Implementation
- **Files:** `core/spreadsheet/Spreadsheet.kt`, `Sheet.kt`, `Cell.kt`
- **Tasks:**
    - [ ] Define the data classes for `Spreadsheet`, `Sheet`, and `Cell`.
    - [ ] Implement the grid model (e.g., using a map or a list of lists) to store cell data.
    - [ ] Implement basic cell value getting/setting logic.

### 2. Basic UI Implementation
- **Files:** `ui/SpreadsheetWindow.kt`, `ui/components/CellGrid.kt`
- **Tasks:**
    - [ ] Create the main application window in `SpreadsheetWindow.kt`.
    - [ ] Implement a basic, scrollable grid of cells in `CellGrid.kt` using Jetpack Compose.
    - [ ] Display cell values from the core model in the UI grid.
    - [ ] Allow basic cell selection.

### 3. Formula Engine (Kotlin Layer)
- **Files:** `core/formula/FormulaEngine.kt`, `core/formula/functions/`
- **Tasks:**
    - [ ] Implement `FormulaEngine.kt` to parse and evaluate simple formulas (e.g., `=A1+B1`).
    - [ ] Implement basic functions like `SUM`, `AVERAGE` in the `functions` directory.

### 4. Python Integration
- **Files:** `python/PythonExecutor.kt`
- **Tasks:**
    - [ ] Implement `PythonExecutor.kt` to initialize the GraalVM Polyglot Context.
    - [ ] Create a method to execute a simple Python script (e.g., `print('hello from python')`) and capture the output.

### 5. File I/O
- **Files:** `io/ExcelReader.kt`, `io/ExcelWriter.kt`
- **Tasks:**
    - [ ] Implement basic `.xlsx` reading using Apache POI to populate the core spreadsheet model.
    - [ ] Implement basic `.xlsx` writing.

---

## Future Tasks (Post-MVP)

- [ ] **GitHub Release:** Create a GitHub Release and upload the Linux package (`.deb`) built during the initial setup.
