package com.meditrack.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY isPrimary DESC, name COLLATE NOCASE")
    fun observeAll(): Flow<List<EmergencyContact>>

    @Query("SELECT * FROM contacts ORDER BY isPrimary DESC")
    suspend fun getAll(): List<EmergencyContact>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(contact: EmergencyContact): Long

    @Delete
    suspend fun delete(contact: EmergencyContact)

    @Query("UPDATE contacts SET isPrimary = 0")
    suspend fun clearPrimary()
}
