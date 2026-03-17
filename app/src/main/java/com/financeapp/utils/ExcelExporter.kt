package com.financeapp.utils

import android.content.Context
import com.financeapp.data.JournalEntry
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object ExcelExporter {

    fun export(context: Context, entries: List<JournalEntry>, title: String = "Journal"): File {
        val ts   = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val name = "${title.replace(Regex("[^A-Za-z0-9_]"), "_")}_$ts.csv"
        val file = File(context.getExternalFilesDir(null), name)

        FileWriter(file).use { fw ->
            // Header
            fw.write("No.,Date,Details,Amount,Type,Vendor\n")

            // Rows
            entries.forEachIndexed { idx, e ->
                val details = e.details.replace(",", ";").replace("\n", " ")
                val vendor  = (e.vendorName ?: "-").replace(",", ";")
                fw.write("${idx + 1},${e.date},$details,${e.amount},${e.type},$vendor\n")
            }

            // Summary
            val totalCredit = entries.filter { it.type == "CREDIT" }.sumOf { it.amount }
            val totalDebit  = entries.filter { it.type == "DEBIT"  }.sumOf { it.amount }
            val balance     = totalCredit - totalDebit
            fw.write("\n")
            fw.write("Total Credit:,${"%.2f".format(totalCredit)}\n")
            fw.write("Total Debit:,${"%.2f".format(totalDebit)}\n")
            fw.write("Net Balance:,${"%.2f".format(balance)}\n")
        }

        return file
    }
}
```

Commit changes.

---

## Fix 3 — Edit `app/proguard-rules.pro`

Go to that file → pencil → delete everything and replace with just:
```
# No special rules needed
