package com.python.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledTaskDao {

    @Query("SELECT * FROM scheduled_tasks")
    fun getAllFlow(): Flow<List<ScheduledTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: ScheduledTask)

    @Delete
    suspend fun delete(task: ScheduledTask)

    @Query("DELETE FROM scheduled_tasks WHERE filePath = :path")
    suspend fun deleteByPath(path: String)
}
