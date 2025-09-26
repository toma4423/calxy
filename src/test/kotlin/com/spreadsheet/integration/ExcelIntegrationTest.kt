package com.spreadsheet.integration

import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream

class ExcelIntegrationTest {

    private val testFilePath = "build/tmp/test.xlsx"

    @Test
    fun `can write to and read from an Excel file`() {
        // 1. Write to an Excel file
        val workbookWrite = XSSFWorkbook()
        val sheet = workbookWrite.createSheet("Test Sheet")
        val row = sheet.createRow(0)
        val cell = row.createCell(0)
        cell.setCellValue("Hello, Excel!")

        // Ensure the directory exists
        File(testFilePath).parentFile.mkdirs()

        FileOutputStream(testFilePath).use { outputStream ->
            workbookWrite.write(outputStream)
        }
        workbookWrite.close()

        val testFile = File(testFilePath)
        assert(testFile.exists()) { "Test Excel file was not created." }

        // 2. Read from the same Excel file
        FileInputStream(testFilePath).use { inputStream ->
            val workbookRead = XSSFWorkbook(inputStream)
            val readSheet = workbookRead.getSheetAt(0)
            val readRow = readSheet.getRow(0)
            val readCell = readRow.getCell(0)
            val cellValue = readCell.stringCellValue

            assertEquals("Hello, Excel!", cellValue)
            workbookRead.close()
        }

        // Clean up the file
        testFile.delete()
    }
}
