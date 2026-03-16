package com.financeapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalEntryDao {

    @Query("SELECT * FROM journal_entries ORDER BY date DESC, id DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries WHERE vendorId = :vendorId ORDER BY date DESC, id DESC")
    fun getEntriesByVendor(vendorId: Int): Flow<List<JournalEntry>>

    @Query("SELECT * FROM journal_entries ORDER BY date DESC, id DESC")
    suspend fun getAllEntriesOnce(): List<JournalEntry>

    @Query("SELECT * FROM journal_entries WHERE vendorId = :vendorId ORDER BY date DESC, id DESC")
    suspend fun getEntriesByVendorOnce(vendorId: Int): List<JournalEntry>

    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): JournalEntry?

    @Insert
    suspend fun insert(entry: JournalEntry)

    @Update
    suspend fun update(entry: JournalEntry)

    @Delete
    suspend fun delete(entry: JournalEntry)
}
