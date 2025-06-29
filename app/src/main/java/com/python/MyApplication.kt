package com.python

import android.app.Application
import android.util.Log
import com.python.data.LogManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {
//    @Inject lateinit var startupSync: StartupSync
    override fun onCreate() {
        Log.d("LogManager", "init called")
        super.onCreate()
        LogManager.init(this)
//        startupSync.sync()
    }
}