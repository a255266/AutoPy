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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
import androidx.lifecycle.viewModelScope
import com.python.data.ScheduledTask
import com.python.data.ScheduledTaskRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ScheduledTaskRepository
) : ViewModel() {


    private val _fileList = MutableStateFlow<List<File>>(emptyList())
    val fileList: StateFlow<List<File>> = _fileList

    fun createFile(context: Context, filename: String) {
        val dir = File(context.filesDir, "python_files")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "$filename.py")
        if (!file.exists()) file.createNewFile()
        loadFiles(context)
    }

    fun loadFiles(context: Context) {
        val dir = File(context.filesDir, "python_files")
        val files = dir.listFiles()?.filter { it.isFile } ?: emptyList()
        _fileList.value = files
    }


    fun deleteFile(file: File, context: Context) {
        if (file.exists()) file.delete()
        loadFiles(context)
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

    // 根据路径删除任务
    fun deleteByPath(path: String) {
        viewModelScope.launch {
            repository.deleteByPath(path)
        }
    }
}
