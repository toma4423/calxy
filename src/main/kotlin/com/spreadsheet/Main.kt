package com.spreadsheet

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.spreadsheet.core.spreadsheet.CellValue
import com.spreadsheet.core.spreadsheet.Sheet

fun main() = application {
    // 1. Create and pre-populate the Sheet data model
    val sheet = Sheet()
    sheet.updateCell(0, 0, "10") // A1
    sheet.updateCell(0, 1, "20") // B1
    sheet.updateCell(0, 2, "=A1+B1") // C1 -> 30.0
    sheet.updateCell(1, 0, "Hello") // A2
    sheet.updateCell(1, 1, "=(2+3)*A1") // B2 -> 50.0
    sheet.updateCell(1, 2, "=A1/0") // C2 -> #DIV/0!
    sheet.updateCell(3, 3, "Another cell") // D4

    Window(
        onCloseRequest = ::exitApplication,
        title = "Calxy Spreadsheet",
        state = rememberWindowState(width = 1024.dp, height = 768.dp)
    ) {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                // 2. Pass the sheet to the UI
                SpreadsheetView(sheet)
            }
        }
    }
}

@Composable
fun SpreadsheetView(sheet: Sheet) {
    val numRows = 100 // Example number of rows
    val numCols = 26  // Example number of columns

    Column(modifier = Modifier.fillMaxSize()) {
        // Column Headers (A, B, C...)
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            item { // Empty corner cell
                Box(modifier = Modifier.width(60.dp).height(30.dp))
            }
            items(numCols) { colIndex ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(100.dp)
                        .height(30.dp)
                        .border(1.dp, Color.Gray)
                ) {
                    Text(toColumnName(colIndex))
                }
            }
        }

        // Rows with Headers and Cells
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(numRows) { rowIndex ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Row Header (1, 2, 3...)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(60.dp)
                            .height(40.dp)
                            .border(1.dp, Color.Gray)
                    ) {
                        Text((rowIndex + 1).toString())
                    }

                    // Cells in the row
                    LazyRow {
                        items(numCols) { colIndex ->
                            // 3. Get cell data from the sheet
                            val cell = sheet.getCell(rowIndex, colIndex)
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(40.dp)
                                    .border(1.dp, Color.LightGray)
                            ) {
                                // 4. Display the formatted cell value
                                Text(formatCellValue(cell?.computedValue))
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatCellValue(cellValue: CellValue?): String {
    return when (cellValue) {
        is CellValue.DoubleValue -> cellValue.value.toString()
        is CellValue.StringValue -> cellValue.value
        is CellValue.ErrorValue -> cellValue.message
        is CellValue.BooleanValue -> cellValue.value.toString().uppercase()
        is CellValue.Empty, null -> ""
    }
}

fun toColumnName(index: Int): String {
    var i = index
    val sb = StringBuilder()
    while (i >= 0) {
        sb.insert(0, ('A' + i % 26))
        i = i / 26 - 1
    }
    return sb.toString()
}
