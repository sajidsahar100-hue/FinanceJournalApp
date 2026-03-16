package com.financeapp.utils

import android.content.Context
import com.financeapp.data.JournalEntry
import org.apache.poi.hssf.usermodel.HSSFWorkbook
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

        val headerFont = wb.createFont().apply {
            bold = true; color = IndexedColors.WHITE.index
        }
        val headerStyle = wb.createCellStyle().apply {
            fillForegroundColor = IndexedColors.DARK_GREEN.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(headerFont)
        }
        val creditStyle = wb.createCellStyle().apply {
            val f = wb.createFont().apply { color = IndexedColors.GREEN.index }
            setFont(f)
        }
        val debitStyle = wb.createCellStyle().apply {
            val f = wb.createFont().apply { color = IndexedColors.RED.index }
            setFont(f)
        }
        val boldStyle = wb.createCellStyle().apply {
            val f = wb.createFont().apply { bold = true }
            setFont(f)
        }

        // Header row
        val headers = listOf("No.", "Date", "Details", "Amount", "Type", "Vendor")
        sheet.createRow(0).also { row ->
            headers.forEachIndexed { i, h ->
                row.createCell(i).apply { setCellValue(h); cellStyle = headerStyle }
            }
        }

        // Data rows
        entries.forEachIndexed { idx, e ->
            sheet.createRow(idx + 1).also { row ->
                row.createCell(0).setCellValue((idx + 1).toDouble())
                row.createCell(1).setCellValue(e.date)
                row.createCell(2).setCellValue(e.details)
                row.createCell(3).apply {
                    setCellValue(e.amount)
                    cellStyle = if (e.type == "CREDIT") creditStyle else debitStyle
                }
                row.createCell(4).apply {
                    setCellValue(e.type)
                    cellStyle = if (e.type == "CREDIT") creditStyle else debitStyle
                }
                row.createCell(5).setCellValue(e.vendorName ?: "-")
            }
        }

        // Auto-size
        (0..5).forEach { sheet.autoSizeColumn(it) }

        // Summary block
        val offset = entries.size + 2
        val totalCredit = entries.filter { it.type == "CREDIT" }.sumOf { it.amount }
        val totalDebit  = entries.filter { it.type == "DEBIT"  }.sumOf { it.amount }
        val balance     = totalCredit - totalDebit

        mapOf(
            offset     to Pair("Total Credit:", totalCredit),
            offset + 1 to Pair("Total Debit:",  totalDebit),
            offset + 2 to Pair("Net Balance:",  balance)
        ).forEach { (rowIdx, pair) ->
            sheet.createRow(rowIdx).also { row ->
                row.createCell(0).apply { setCellValue(pair.first); cellStyle = boldStyle }
                row.createCell(1).apply {
                    setCellValue(pair.second)
                    cellStyle = when {
                        rowIdx == offset     -> creditStyle
                        rowIdx == offset + 1 -> debitStyle
                        pair.second >= 0     -> creditStyle
                        else                 -> debitStyle
                    }
                }
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
