package com.python.data

import android.util.Log
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

    @Query("DELETE FROM scheduled_tasks WHERE id = :id")
    suspend fun deleteById(id: Int)
}


@Dao
interface SyncFileDao {
    @Query("SELECT * FROM file_sync")
    suspend fun getAll(): List<FileSyncEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FileSyncEntry) // 删除方法体和日志

    @Delete
    suspend fun delete(entity: FileSyncEntry)

    @Query("DELETE FROM file_sync WHERE fileName = :name")
    suspend fun deleteByName(name: String)

    @Query("SELECT * FROM file_sync")
    fun observeAll(): Flow<List<FileSyncEntry>>

    @Query("SELECT * FROM file_sync WHERE fileName = :name LIMIT 1")
    suspend fun getByFileName(name: String): FileSyncEntry?


}
