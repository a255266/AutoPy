package com.python.service

import android.util.Log
import kotlinx.coroutines.*
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class TaskScheduler(private val scope: CoroutineScope) {
    private val taskMap = mutableMapOf<String, Job>()

    fun scheduleDaily(taskId: String, hour: Int, minute: Int, task: suspend () -> Unit) {
        if (taskMap.containsKey(taskId)) return

        Log.d("ForegroundService", "添加任务：$taskId，时间：$hour:$minute")

        val job = scope.launch {
            while (isActive) {
                val now = LocalDateTime.now()
                var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
                if (next.isBefore(now)) next = next.plusDays(1)

                val delayMillis = Duration.between(now, next).toMillis()
                Log.d("ForegroundService", "任务 $taskId 延迟 $delayMillis 毫秒后执行")
                delay(delayMillis)

                try {
                    Log.d("ForegroundService", "执行任务：$taskId，当前时间：${LocalDateTime.now()}")
                    task()
                } catch (e: Exception) {
                    Log.e("ForegroundService", "任务 $taskId 执行异常：${e.message}", e)
                }
            }
        }

        taskMap[taskId] = job
    }

    fun scheduleOnce(taskId: String, hour: Int, minute: Int, task: suspend () -> Unit) {
        if (taskMap.containsKey(taskId)) return

        Log.d("ForegroundService", "添加一次性任务：$taskId，时间：$hour:$minute")

        val job = scope.launch {
            val now = LocalDateTime.now()
            var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
            if (next.isBefore(now)) next = next.plusDays(1)

            val delayMillis = Duration.between(now, next).toMillis()
            Log.d("ForegroundService", "一次性任务 $taskId 延迟 $delayMillis 毫秒后执行")
            delay(delayMillis)

            try {
                Log.d("ForegroundService", "执行一次性任务：$taskId，当前时间：${LocalDateTime.now()}")
                task()
            } catch (e: Exception) {
                Log.e("ForegroundService", "任务 $taskId 执行异常：${e.message}", e)
            } finally {
                taskMap.remove(taskId)
            }
        }

        taskMap[taskId] = job
    }

    fun cancel(taskId: String) {
        taskMap[taskId]?.cancel()
        taskMap.remove(taskId)
    }

    fun cancelAll() {
        taskMap.values.forEach { it.cancel() }
        taskMap.clear()
    }
}
