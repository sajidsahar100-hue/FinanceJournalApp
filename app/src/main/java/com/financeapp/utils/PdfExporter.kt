package com.financeapp.utils

import android.content.Context
import android.graphics.*
import android.graphics.pdf.PdfDocument
import com.financeapp.data.JournalEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfExporter {
    private const val PW = 595
    private const val PH = 842
    private const val M  = 36f
    private const val RH = 22f
    private val COLS = floatArrayOf(30f, 78f, 185f, 82f, 58f, 90f) // No, Date, Details, Amount, Type, Vendor

    fun export(context: Context, entries: List<JournalEntry>, title: String = "Journal"): File {
        val doc = PdfDocument()
        var pgN = 1

        fun newPage(): Pair<PdfDocument.Page, Canvas> {
            val p = doc.startPage(PdfDocument.PageInfo.Builder(PW, PH, pgN++).create())
            return Pair(p, p.canvas)
        }

        var (page, cv) = newPage()
        var y = M

        // Paints
        val titleP  = Paint().apply { textSize = 20f; color = Color.rgb(27,94,32); isFakeBoldText = true }
        val subP    = Paint().apply { textSize = 9f;  color = Color.GRAY }
        val hdrBgP  = Paint().apply { color = Color.rgb(27,94,32) }
        val hdrTxtP = Paint().apply { textSize = 10f; color = Color.WHITE; isFakeBoldText = true }
        val cellP   = Paint().apply { textSize = 9f;  color = Color.BLACK }
        val lineP   = Paint().apply { color = Color.LTGRAY; strokeWidth = 0.5f }
        val boldP   = Paint().apply { textSize = 11f; isFakeBoldText = true; color = Color.BLACK }
        val greenP  = Paint().apply { textSize = 11f; color = Color.rgb(46,125,50); isFakeBoldText = true }
        val redP    = Paint().apply { textSize = 11f; color = Color.rgb(198,40,40); isFakeBoldText = true }
        val gCell   = Paint().apply { textSize = 9f;  color = Color.rgb(46,125,50) }
        val rCell   = Paint().apply { textSize = 9f;  color = Color.rgb(198,40,40) }

        fun drawHeader(canvas: Canvas, yy: Float) {
            canvas.drawRect(M, yy, PW - M, yy + RH, hdrBgP)
            var x = M + 3
            listOf("No.","Date","Details","Amount","Type","Vendor").forEachIndexed { i, h ->
                canvas.drawText(h, x, yy + 15f, hdrTxtP)
                x += COLS[i]
            }
        }

        // Title block
        cv.drawText(title, M, y + 18f, titleP)
        y += 30f
        val ts = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
        cv.drawText("Generated: $ts    Entries: ${entries.size}", M, y, subP)
        y += 20f

        drawHeader(cv, y); y += RH

        entries.forEachIndexed { idx, e ->
            if (y > PH - M - RH * 2) {
                doc.finishPage(page); val np = newPage(); page = np.first; cv = np.second; y = M
                drawHeader(cv, y); y += RH
            }
            val isCredit = e.type == "CREDIT"
            var x = M + 3
            val rowData = listOf(
                (idx + 1).toString(),
                e.date,
                if (e.details.length > 28) e.details.take(25) + "…" else e.details,
                "%,.2f".format(e.amount),
                e.type,
                if ((e.vendorName ?: "-").length > 13) (e.vendorName ?: "-").take(10) + "…" else (e.vendorName ?: "-")
            )
            rowData.forEachIndexed { i, v ->
                val paint = if (i == 3 || i == 4) (if (isCredit) gCell else rCell) else cellP
                cv.drawText(v, x, y + 15f, paint)
                x += COLS[i]
            }
            if (idx % 2 == 1) cv.drawRect(M, y, PW - M, y + RH, Paint().apply { color = Color.argb(20, 0,0,0) })
            cv.drawLine(M, y + RH, PW - M, y + RH, lineP)
            y += RH
        }

        // Summary
        y += 18f
        if (y > PH - M - 70) {
            doc.finishPage(page); val np = newPage(); page = np.first; cv = np.second; y = M
        }
        val tCredit = entries.filter { it.type == "CREDIT" }.sumOf { it.amount }
        val tDebit  = entries.filter { it.type == "DEBIT"  }.sumOf { it.amount }
        val bal     = tCredit - tDebit
        cv.drawLine(M, y, PW - M, y, Paint().apply { color = Color.DKGRAY; strokeWidth = 1f }); y += 14f
        cv.drawText("Total Credit :", M, y, boldP); cv.drawText("+%,.2f".format(tCredit), M + 140, y, greenP); y += 20f
        cv.drawText("Total Debit  :", M, y, boldP); cv.drawText("-%,.2f".format(tDebit),  M + 140, y, redP);   y += 20f
        cv.drawText("Net Balance  :", M, y, boldP)
        cv.drawText("%,.2f".format(bal), M + 140, y, if (bal >= 0) greenP else redP)

        doc.finishPage(page)

        val fname = "${title.replace(Regex("[^A-Za-z0-9_]"), "_")}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
        val file = File(context.getExternalFilesDir(null), fname)
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        return file
    }
}
