package com.python.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase



@Database(entities = [ScheduledTask::class], version = 1)
abstract class ScheduledTaskDatabase : RoomDatabase() {
    abstract fun scheduledTaskDao(): ScheduledTaskDao

    companion object {
        @Volatile private var INSTANCE: ScheduledTaskDatabase? = null

        fun getInstance(context: Context): ScheduledTaskDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ScheduledTaskDatabase::class.java,
                    "scheduled_tasks.db"
                ).build().also { INSTANCE = it }
            }
    }
}
