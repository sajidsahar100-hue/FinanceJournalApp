package com.financeapp.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.financeapp.data.*
import com.financeapp.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class JournalViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = JournalRepository(AppDatabase.getDatabase(app))

    val allEntries: LiveData<List<JournalEntry>> = repo.allEntries.asLiveData()
    val allVendors: LiveData<List<Vendor>>        = repo.allVendors.asLiveData()

    fun getEntriesByVendor(vendorId: Int): Flow<List<JournalEntry>> =
        repo.getEntriesByVendor(vendorId)

    fun insertEntry(e: JournalEntry) = viewModelScope.launch { repo.insertEntry(e) }
    fun updateEntry(e: JournalEntry) = viewModelScope.launch { repo.updateEntry(e) }
    fun deleteEntry(e: JournalEntry) = viewModelScope.launch { repo.deleteEntry(e) }

    suspend fun getEntryById(id: Int): JournalEntry? = repo.getEntryById(id)

    fun addVendor(name: String) = viewModelScope.launch { repo.insertVendor(Vendor(name = name)) }
    fun deleteVendor(v: Vendor) = viewModelScope.launch { repo.deleteVendor(v) }

    suspend fun getAllEntriesOnce() = repo.getAllEntriesOnce()
    suspend fun getEntriesByVendorOnce(id: Int) = repo.getEntriesByVendorOnce(id)
}
