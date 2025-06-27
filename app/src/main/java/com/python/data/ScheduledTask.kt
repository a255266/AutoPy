package com.python.data

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_tasks")
data class ScheduledTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String,
    val hour: Int,
    val minute: Int,
    val repeatDaily: Boolean
)