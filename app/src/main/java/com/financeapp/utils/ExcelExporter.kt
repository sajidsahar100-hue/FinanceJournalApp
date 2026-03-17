package com.financeapp.utils

import android.content.Context
import com.financeapp.data.JournalEntry
import org.apache.poi.hssf.usermodel.HSSFCellStyle
import org.apache.poi.hssf.usermodel.HSSFFont
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.IndexedColors
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {

    fun export(context: Context, entries: List<JournalEntry>, title: String = "Journal"): File {
        val wb = HSSFWorkbook()
        val sheet = wb.createSheet(title.take(30))

        fun makeStyle(bgColor: Short? = null, fontColor: Short? = null, bold: Boolean = false): HSSFCellStyle {
            val font: HSSFFont = wb.createFont()
            font.bold = bold
            if (fontColor != null) font.color = fontColor
            val style: HSSFCellStyle = wb.createCellStyle()
            style.setFont(font)
            if (bgColor != null) {
                style.fillForegroundColor = bgColor
                style.fillPattern = HSSFCellStyle.SOLID_FOREGROUND
            }
            return style
        }

        val headerStyle = makeStyle(
            bgColor   = IndexedColors.DARK_GREEN.index,
            fontColor = IndexedColors.WHITE.index,
            bold      = true
        )
        val creditStyle = makeStyle(fontColor = IndexedColors.GREEN.index)
        val debitStyle  = makeStyle(fontColor = IndexedColors.RED.index)
        val boldStyle   = makeStyle(bold = true)

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

        (0..5).forEach { sheet.autoSizeColumn(it) }

        // Summary rows
        val offset      = entries.size + 2
        val totalCredit = entries.filter { it.type == "CREDIT" }.sumOf { it.amount }
        val totalDebit  = entries.filter { it.type == "DEBIT"  }.sumOf { it.amount }
        val balance     = totalCredit - totalDebit

        listOf(
            Triple("Total Credit:", totalCredit, creditStyle),
            Triple("Total Debit:",  totalDebit,  debitStyle),
            Triple("Net Balance:",  balance,     if (balance >= 0) creditStyle else debitStyle)
        ).forEachIndexed { i, (label, value, style) ->
            val row = sheet.createRow(offset + i)
            val lc = row.createCell(0)
            lc.setCellValue(label)
            lc.cellStyle = boldStyle
            val vc = row.createCell(1)
            vc.setCellValue(value)
            vc.cellStyle = style
        }

        val ts   = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val name = "${title.replace(Regex("[^A-Za-z0-9_]"), "_")}_$ts.xls"
        val file = File(context.getExternalFilesDir(null), name)
        FileOutputStream(file).use { wb.write(it) }
        wb.close()
        return file
    }
}
