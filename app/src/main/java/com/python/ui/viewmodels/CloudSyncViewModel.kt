package com.python.ui.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.python.data.FileSyncEntry
import com.python.data.SyncFileDao
import com.python.data.WebDavManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CloudSyncViewModel @Inject constructor(
    private val webDavManager: WebDavManager,
    private val syncFileDao: SyncFileDao
) : ViewModel() {

//    val syncEntriesFlow: Flow<List<FileSyncEntry>> = syncFileDao.observeAll()
//        .distinctUntilChanged()



    fun uploadFile(remotePath: String, file: File): Boolean {
        var success = false
        viewModelScope.launch {
            success = webDavManager.upload(remotePath, file)
        }
        return success
    }



    fun uploadAndReplaceFile(file: File) {
        val fileName = file.name
        val nameWithoutExt = file.nameWithoutExtension
        val timestamp = System.currentTimeMillis()
        val newFullName = "${nameWithoutExt}_$timestamp.py"
        val remotePath = "python_files/$newFullName"

        viewModelScope.launch {
            try {
                // 查询旧文件记录
                val oldEntry = syncFileDao.getByFileName(fileName)
                if (oldEntry != null) {
                    val oldRemotePath = "python_files/${oldEntry.fullNameWithTimestamp}"
                    webDavManager.delete(oldRemotePath)
                }
                syncFileDao.insert(
                    FileSyncEntry(
                        fileName = file.name,
                        fullNameWithTimestamp = newFullName,
                        timestamp = timestamp
                    )
                )
                // 上传新文件
                val success = webDavManager.upload(remotePath, file)
                if (success) {
                    Log.d("CloudSync", "✅ 上传成功：$newFullName")
                } else {
                    Log.w("CloudSync", "⚠️ 上传失败：$remotePath")
                }
            } catch (e: Exception) {
                Log.e("CloudSync", "❌ 上传或数据库写入异常", e)
            }
        }
    }

    fun syncNow(context: Context, onComplete: () -> Unit) {
        viewModelScope.launch {
            webDavManager.incrementalSync(context, syncFileDao) {
                onComplete() // ✅ 同步完成后通知 UI
            }
        }
    }



    fun removeSyncEntry(fileName: String) {
        viewModelScope.launch {
            syncFileDao.deleteByName(fileName)
        }
    }



    private fun getLocalDir(): File {
        // 替换为你的本地存储目录路径
        return File("/com.python/files/python_files")
    }
}
