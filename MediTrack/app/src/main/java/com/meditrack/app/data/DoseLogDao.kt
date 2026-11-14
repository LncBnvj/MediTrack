package com.meditrack.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DoseLogDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(log: DoseLog): Long

    @Query("SELECT * FROM dose_logs WHERE medicineId = :medicineId AND scheduledTime = :scheduledTime LIMIT 1")
    suspend fun find(medicineId: Long, scheduledTime: Long): DoseLog?

    @Query("SELECT * FROM dose_logs WHERE id = :id")
    suspend fun getById(id: Long): DoseLog?

    @Query("UPDATE dose_logs SET status = :status, actionTime = :time WHERE id = :id")
    suspend fun setStatus(id: Long, status: DoseStatus, time: Long?)

    @Query("SELECT * FROM dose_logs WHERE scheduledTime BETWEEN :from AND :to ORDER BY scheduledTime")
    fun observeBetween(from: Long, to: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE scheduledTime >= :from AND status != 'PENDING' ORDER BY scheduledTime DESC")
    fun observeSince(from: Long): Flow<List<DoseLog>>

    @Query("SELECT * FROM dose_logs WHERE status = 'PENDING'")
    suspend fun getPending(): List<DoseLog>

    @Query("SELECT * FROM dose_logs WHERE status = 'PENDING' AND scheduledTime < :before")
    suspend fun getOverduePending(before: Long): List<DoseLog>

    @Query("SELECT * FROM dose_logs ORDER BY scheduledTime DESC LIMIT :limit")
    suspend fun lastN(limit: Int): List<DoseLog>

    @Query("DELETE FROM dose_logs WHERE medicineId = :medicineId AND status = 'PENDING'")
    suspend fun deletePendingFor(medicineId: Long)
}
