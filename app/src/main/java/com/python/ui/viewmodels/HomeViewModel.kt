package com.python.ui.viewmodels

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject
import android.content.Context
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import androidx.lifecycle.viewModelScope
import com.python.StartupSync
import com.python.data.FileSyncEntry
import com.python.data.ScheduledTask
import com.python.data.ScheduledTaskDao
import com.python.data.ScheduledTaskRepository
import com.python.data.SyncFileDao
import com.python.data.WebDavManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ScheduledTaskRepository,
    private val dao: ScheduledTaskDao,
    private val webDavManager: WebDavManager,
    private val syncFileDao: SyncFileDao,
    private val startupSync: StartupSync,
    @ApplicationContext private val context: Context,
) : ViewModel() {


    private val _fileList = MutableStateFlow<List<File>>(emptyList())
    val fileList: StateFlow<List<File>> = _fileList


    fun createFile(context: Context, filename: String) {
        val dir = File(context.filesDir, "python_files")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$filename.py")
        if (!file.exists()) file.createNewFile()
        loadFiles()
    }

    fun loadFiles() {
        val dir = File(context.filesDir, "python_files")
        val files = dir.listFiles()?.filter { it.isFile } ?: emptyList()
        _fileList.value = files
        Log.d("WebDAV", "刷新本地文件列表，文件数：${files.size}")
    }


//    init {
//        // 监听启动同步完成事件，刷新本地文件列表
//        viewModelScope.launch {
//            startupSync.syncCompleted.collect {
//                Log.d("CloudSyncViewModel", "收到同步完成事件，刷新文件列表")
//                loadFiles(context)
//            }
//        }
//    }

    // 可以调用这个方法触发同步
//    fun startSync() {
//        startupSync.sync()
//    }

    fun loadLocalFiles(context: Context): List<File> {
        val localDir = File(context.filesDir, "python_files")
        if (!localDir.exists()) localDir.mkdirs()
        return localDir.listFiles()?.toList() ?: emptyList()
    }

    fun deleteFile(file: File, context: Context) {
        viewModelScope.launch {
            try {
                val fileName = file.name

                // 从数据库中查找记录
                val entry = syncFileDao.getByFileName(fileName)

                // 删除本地文件
                if (file.exists()) file.delete()

                // 删除云端对应文件（如果有记录）
                entry?.let {
                    val remotePath = "python_files/${it.fullNameWithTimestamp}"
                    webDavManager.delete(remotePath)
                    syncFileDao.deleteByName(fileName) // 删除数据库记录
                    Log.d("CloudSync", "✅ 已删除本地、云端、数据库记录：$fileName")
                }

                // 刷新本地列表
                loadFiles()

            } catch (e: Exception) {
                Log.e("CloudSync", "❌ 删除文件失败", e)
            }
        }
    }


    val scheduledTasks = repository.allTasksFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // 所有定时任务的 Flow
    val allTasks = repository.allTasksFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 插入任务
    fun insertTask(task: ScheduledTask) {
        viewModelScope.launch {
            repository.insert(task)
        }
    }

    // 删除任务
    fun deleteTask(task: ScheduledTask) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }



    fun updateTask(task: ScheduledTask) = viewModelScope.launch {
        dao.insert(task)  // Room 的 insert(onConflict=REPLACE) 也可以更新
    }


    fun performIncrementalSync(context: Context) {
        viewModelScope.launch {
            try {
                val cloudList = webDavManager.listCloudPythonFiles() // [xxx_时间戳.py, ...]
                val localEntries = syncFileDao.getAll()
                val localFullNames = localEntries.map { it.fullNameWithTimestamp }

                val localMap = localEntries.associateBy { it.fullNameWithTimestamp }

                val localDir = File(context.filesDir, "python_files")
                if (!localDir.exists()) localDir.mkdirs()

                // === 从云端下载本地缺失的 ===
                val toDownload = cloudList.filterNot { it in localFullNames }
                toDownload.forEach { fullName ->
                    val localName = fullName.substringBeforeLast("_") + ".py"
                    val localFile = File(localDir, localName)
                    val remotePath = "python_files/$fullName"
                    val success = webDavManager.download(remotePath, localFile)
                    if (success) {
                        Log.d("CloudSync", "✅ 下载：$localName")
                        val timestamp = extractTimestamp(fullName)
                        syncFileDao.insert(FileSyncEntry(localName, fullName, timestamp))
                    }
                }

                // === 从本地上传云端缺失的 ===
                val toUpload = localEntries.filterNot { it.fullNameWithTimestamp in cloudList }
                toUpload.forEach { entry ->
                    val localFile = File(localDir, entry.fileName)
                    if (localFile.exists()) {
                        val remotePath = "python_files/${entry.fullNameWithTimestamp}"
                        val success = webDavManager.upload(remotePath, localFile)
                        if (success) {
                            Log.d("CloudSync", "☁️ 补传：${entry.fileName}")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e("CloudSync", "❌ 增量同步失败", e)
            }
        }
    }

    private fun extractTimestamp(name: String): Long {
        return name.substringAfterLast("_").removeSuffix(".py").toLongOrNull() ?: 0L
    }


}
