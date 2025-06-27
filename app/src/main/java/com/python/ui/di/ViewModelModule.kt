package com.python.ui.di

import android.content.Context
import com.python.data.ScheduledTaskDao
import com.python.data.ScheduledTaskDatabase
import com.python.data.ScheduledTaskRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HomeViewModule {

    @Provides
    @Singleton
    fun provideScheduledTaskDatabase(@ApplicationContext context: Context): ScheduledTaskDatabase {
        return ScheduledTaskDatabase.getInstance(context)
    }

    @Provides
    fun provideScheduledTaskDao(db: ScheduledTaskDatabase): ScheduledTaskDao {
        return db.scheduledTaskDao()
    }

    @Provides
    fun provideScheduledTaskRepository(dao: ScheduledTaskDao): ScheduledTaskRepository {
        return ScheduledTaskRepository(dao)
    }
}
