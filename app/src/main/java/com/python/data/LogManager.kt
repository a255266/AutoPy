package com.python.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File
import kotlinx.coroutines.launch

import java.io.IOException
object LogManager {

    private const val TAG = "LogManager"
    private const val LOG_DIR = "python_logs"
    private const val LOG_FILE_NAME = "python_run_log.txt"

    private lateinit var appContext: Context
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private val _logFlow = MutableStateFlow("")
    val logFlow: StateFlow<String> get() = _logFlow


    private val logFile: File
        get() {
            val logDir = File(appContext.filesDir, LOG_DIR).apply {
                if (!exists()) mkdirs()
            }
            return File(logDir, LOG_FILE_NAME)
        }

    // 线程安全初始化
    @Synchronized
    fun init(context: Context) {
        if (!this::appContext.isInitialized) {
            appContext = context.applicationContext
            Log.d(TAG, "init called")
        }
    }

    fun loadLog() {
        ioScope.launch {
            try {
                val content = if (logFile.exists()) {
                    logFile.readText()
                } else {
                    ""
                }
                _logFlow.emit(content)
            } catch (e: IOException) {
                Log.e(TAG, "loadLog failed", e)
                _logFlow.emit("日志读取失败：${e.message}")
            }
        }
    }
//    fun append(text: String) {
//        Log.d("LogManager", "append called, appContext initialized? ${::appContext.isInitialized}")
//        val logFile = File(appContext.filesDir, "python_logs/python_run_log.txt")
//        try {
//            logFile.appendText(text)
//            Log.d("LogManager", "已写入日志: $text")
//            _logFlow.update { it + text }
//        } catch (e: Exception) {
//            Log.e("LogManager", "写入日志失败", e)
//        }
//    }
    fun append(text: String) {
        Log.d("LogManager", "append called, appContext initialized? ${::appContext.isInitialized}")
        if (!this::appContext.isInitialized) {
            Log.e(TAG, "append called before init!")
            return
        }
        try {
            val logDir = File(appContext.filesDir, LOG_DIR).apply { if (!exists()) mkdirs() }
            val logFile = File(logDir, LOG_FILE_NAME)
            logFile.appendText(text)
        } catch (e: Exception) {
            Log.e(TAG, "写日志异常", e)
        }
    }


    fun clear() {
        _logFlow.value = ""
        try {
            logFile.writeText("")
        } catch (e: Exception) {
            Log.e(TAG, "清除日志失败", e)
        }
    }

    fun current(): String = _logFlow.value
}
