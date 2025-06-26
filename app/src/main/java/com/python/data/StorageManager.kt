package com.python.data

import android.content.Context
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object LocalStorageManager {

    private const val LOG_FILENAME = "autopy_log.txt"
    private const val CODE_DIR = "code"

    // 保存代码内容到文件（以文件名保存）
    fun saveCode(context: Context, filename: String, content: String): Boolean {
        return try {
            val dir = File(context.filesDir, CODE_DIR).apply { mkdirs() }
            val file = File(dir, "$filename.py")
            file.writeText(content)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 将运行日志追加写入日志文件
    fun appendLog(context: Context, logContent: String) {
        try {
            val logFile = File(context.filesDir, LOG_FILENAME)
            val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val entry = "\n[$timestamp]\n$logContent\n"
            FileOutputStream(logFile, true).bufferedWriter().use {
                it.append(entry)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 可选：读取日志内容
    fun readLog(context: Context): String {
        return try {
            val logFile = File(context.filesDir, LOG_FILENAME)
            if (logFile.exists()) logFile.readText() else ""
        } catch (e: Exception) {
            e.printStackTrace()
            "读取日志失败"
        }
    }
}
