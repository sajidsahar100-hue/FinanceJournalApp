package com.financeapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,           // "YYYY-MM-DD"
    val details: String,
    val amount: Double,
    val type: String,           // "CREDIT" | "DEBIT"
    val vendorId: Int? = null,
    val vendorName: String? = null
)
