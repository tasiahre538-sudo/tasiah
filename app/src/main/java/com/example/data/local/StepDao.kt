package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StepRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM step_history ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<StepRecord>>

    @Query("SELECT * FROM step_history WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getRecordsSince(sinceTimestamp: Long): Flow<List<StepRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: StepRecord): Long

    @Query("DELETE FROM step_history WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM step_history")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM step_history")
    suspend fun getCount(): Int
}
