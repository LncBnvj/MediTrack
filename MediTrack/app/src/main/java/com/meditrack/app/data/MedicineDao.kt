package com.meditrack.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM medicines ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines")
    suspend fun getAll(): List<Medicine>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getById(id: Long): Medicine?

    @Insert
    suspend fun insert(medicine: Medicine): Long

    @Update
    suspend fun update(medicine: Medicine)

    @Delete
    suspend fun delete(medicine: Medicine)

    @Query("UPDATE medicines SET stockLeft = stockLeft - 1 WHERE id = :id AND stockLeft IS NOT NULL AND stockLeft > 0")
    suspend fun decrementStock(id: Long)
}
