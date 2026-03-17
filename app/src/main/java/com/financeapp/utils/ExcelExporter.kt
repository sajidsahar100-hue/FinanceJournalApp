package com.financeapp.utils

import android.content.Context
import com.financeapp.data.JournalEntry
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellStyle
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {

    fun export(context: Context, entries: List<JournalEntry>, title: String = "Journal"): File {
        val wb    = HSSFWorkbook()
        val sheet = wb.createSheet(title.take(30))

        // Header style
        val headerFont = wb.createFont()
        headerFont.bold = true
        headerFont.color = IndexedColors.WHITE.index
        val headerStyle: CellStyle = wb.createCellStyle()
        headerStyle.fillForegroundColor = IndexedColors.DARK_GREEN.index
        headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
        headerStyle.setFont(headerFont)

        // Credit style
        val creditFont = wb.createFont()
        creditFont.color = IndexedColors.GREEN.index
        val creditStyle: CellStyle = wb.createCellStyle()
        creditStyle.setFont(creditFont)

        // Debit style
        val debitFont = wb.createFont()
        debitFont.color = IndexedColors.RED.index
        val debitStyle: CellStyle = wb.createCellStyle()
        debitStyle.setFont(debitFont)

        // Bold style
        val boldFont = wb.createFont()
        boldFont.bold = true
        val boldStyle: CellStyle = wb.createCellStyle()
        boldStyle.setFont(boldFont)

        // Header row
        val headers = listOf("No.", "Date", "Details", "Amount", "Type", "Vendor")
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, h ->
            val cell = headerRow.createCell(i)
            cell.setCellValue(h)
            cell.cellStyle = headerStyle
        }

        // Data rows
        entries.forEachIndexed { idx, e ->
            val row = sheet.createRow(idx + 1)
            row.createCell(0).setCellValue((idx + 1).toDouble())
            row.createCell(1).setCellValue(e.date)
            row.createCell(2).setCellValue(e.details)
            val amtCell = row.createCell(3)
            amtCell.setCellValue(e.amount)
            amtCell.cellStyle = if (e.type == "CREDIT") creditStyle else debitStyle
            val typeCell = row.createCell(4)
            typeCell.setCellValue(e.type)
            typeCell.cellStyle = if (e.type == "CREDIT") creditStyle else debitStyle
            row.createCell(5).setCellValue(e.vendorName ?: "-")
        }

        // Auto-size columns
        (0..5).forEach { sheet.autoSizeColumn(it) }

        // Summary
        val offset = entries.size + 2
        val totalCredit = entries.filter { it.type == "CREDIT" }.sumOf { it.amount }
        val totalDebit  = entries.filter { it.type == "DEBIT"  }.sumOf { it.amount }
        val balance     = totalCredit - totalDebit

        val summaryData = listOf(
            Pair("Total Credit:", totalCredit),
            Pair("Total Debit:",  totalDebit),
            Pair("Net Balance:",  balance)
        )
        summaryData.forEachIndexed { i, pair ->
            val row = sheet.createRow(offset + i)
            val labelCell = row.createCell(0)
            labelCell.setCellValue(pair.first)
            labelCell.cellStyle = boldStyle
            val valCell = row.createCell(1)
            valCell.setCellValue(pair.second)
            valCell.cellStyle = when {
                i == 0 -> creditStyle
                i == 1 -> debitStyle
                pair.second >= 0 -> creditStyle
                else -> debitStyle
            }
        }

        val ts   = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val name = "${title.replace(Regex("[^A-Za-z0-9_]"), "_")}_$ts.xls"
        val file = File(context.getExternalFilesDir(null), name)
        FileOutputStream(file).use { wb.write(it) }
        wb.close()
        return file
    }
}
