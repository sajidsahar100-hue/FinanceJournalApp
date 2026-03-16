package com.financeapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface VendorDao {

    @Query("SELECT * FROM vendors ORDER BY name ASC")
    fun getAllVendors(): Flow<List<Vendor>>

    @Insert
    suspend fun insert(vendor: Vendor): Long

    @Delete
    suspend fun delete(vendor: Vendor)
}
