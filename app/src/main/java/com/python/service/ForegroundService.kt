package com.python.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.python.R
import android.app.*
import android.os.PowerManager
import android.util.Log
import com.chaquo.python.Python
import com.python.PythonRunner
import com.python.data.ScheduledTaskDao
import com.python.data.ScheduledTaskDatabase
import kotlinx.coroutines.*
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import com.chaquo.python.android.AndroidPlatform

class ForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var scheduler: TaskScheduler
    private lateinit var dao: ScheduledTaskDao
    private var job: Job? = null

    companion object {
        const val CHANNEL_ID = "AutoPyForegroundServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()

        if (!Python.isStarted()) {
            Log.d("ForegroundService", "重新初始化python")
            Python.start(AndroidPlatform(this))
        }

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())

        scheduler = TaskScheduler(serviceScope)
        dao = ScheduledTaskDatabase.getInstance(applicationContext).scheduledTaskDao()

        job = serviceScope.launch {
            dao.getAllFlow().collect { tasks ->
                Log.d("ForegroundService", "监听到数据库任务更新，共 ${tasks.size} 条")

                scheduler.cancelAll()
                Log.d("ForegroundService", "已取消所有旧任务")

                tasks.forEach { task ->
                    val taskId = task.id.toString()
                    Log.d("ForegroundService", "准备调度任务: id=$taskId, 时间=${task.hour}:${task.minute}, 重复=${task.repeatDaily}, 路径=${task.filePath}")

                    if (task.repeatDaily) {
                        scheduler.scheduleDaily(taskId, task.hour, task.minute) {
                            Log.d("ForegroundService", "正在执行任务 id=$taskId 路径=${task.filePath}")
                            runPython(task.filePath)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job?.cancel()
        scheduler.cancelAll()
        serviceScope.cancel()
    }

    private fun runPython(path: String) {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "AutoPy:WakeLock"
        )
        try {
            wakeLock.acquire(10 * 60 * 1000L)
            Log.d("ForegroundService", "正在执行PythonRunner")
            // 执行指定 Python 脚本文件
            PythonRunner.runFile(applicationContext, File(path))

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (wakeLock.isHeld) wakeLock.release()
        }
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AutoPy 前台服务",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, Class.forName("com.python.MainActivity"))
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AutoPy 正在运行")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?) = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY
}
