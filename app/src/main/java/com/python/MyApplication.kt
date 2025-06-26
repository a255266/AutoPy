package com.python

import android.app.Application
import android.util.Log
import com.python.data.LogManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        Log.d("LogManager", "init called")
        super.onCreate()
        LogManager.init(this)
    }
}