package com.financeapp.repository

import com.financeapp.data.*
import kotlinx.coroutines.flow.Flow

class JournalRepository(private val db: AppDatabase) {

    val allEntries: Flow<List<JournalEntry>> = db.journalEntryDao().getAllEntries()
    val allVendors: Flow<List<Vendor>>        = db.vendorDao().getAllVendors()

    fun getEntriesByVendor(vendorId: Int): Flow<List<JournalEntry>> =
        db.journalEntryDao().getEntriesByVendor(vendorId)

    suspend fun insertEntry(e: JournalEntry)  = db.journalEntryDao().insert(e)
    suspend fun updateEntry(e: JournalEntry)  = db.journalEntryDao().update(e)
    suspend fun deleteEntry(e: JournalEntry)  = db.journalEntryDao().delete(e)
    suspend fun getEntryById(id: Int): JournalEntry? = db.journalEntryDao().getById(id)

    suspend fun insertVendor(v: Vendor): Long = db.vendorDao().insert(v)
    suspend fun deleteVendor(v: Vendor)       = db.vendorDao().delete(v)

    suspend fun getAllEntriesOnce(): List<JournalEntry> =
        db.journalEntryDao().getAllEntriesOnce()

    suspend fun getEntriesByVendorOnce(vendorId: Int): List<JournalEntry> =
        db.journalEntryDao().getEntriesByVendorOnce(vendorId)
}
