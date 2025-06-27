package com.python.data

import kotlinx.coroutines.flow.Flow

import com.python.data.ScheduledTask
import com.python.data.ScheduledTaskDao

class ScheduledTaskRepository(private val dao: ScheduledTaskDao) {

    val allTasksFlow: Flow<List<ScheduledTask>> = dao.getAllFlow()

    suspend fun insert(task: ScheduledTask) = dao.insert(task)

    suspend fun delete(task: ScheduledTask) = dao.delete(task)

    suspend fun deleteByPath(path: String) = dao.deleteByPath(path)
}
