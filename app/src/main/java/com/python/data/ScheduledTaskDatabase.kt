package com.python.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors


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

@Database(entities = [FileSyncEntry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun syncFileDao(): SyncFileDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "sync_files.db"
                )
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("AppDatabase", "✅ sync_files.db 创建成功！")
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}


