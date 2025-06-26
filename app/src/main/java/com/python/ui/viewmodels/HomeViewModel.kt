package com.python.ui.viewmodels

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import javax.inject.Inject


@HiltViewModel
class HomeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val runningStates = mutableMapOf<String, MutableStateFlow<Boolean>>()

    fun markRunning(path: String, isRunning: Boolean) {
        runningStates.getOrPut(path) { MutableStateFlow(false) }.value = isRunning
    }

    fun runningMapState(path: String): StateFlow<Boolean> {
        return runningStates.getOrPut(path) { MutableStateFlow(false) }
    }

    // ViewModel
    private val _visibleMap = mutableStateMapOf<String, Boolean>()
    val visibleMap: Map<String, Boolean> get() = _visibleMap

    fun setVisible(path: String, visible: Boolean) {
        _visibleMap[path] = visible
    }

    fun ensureFileKeys(files: List<File>) {
        for (file in files) {
            val path = file.absolutePath
            runningStates.putIfAbsent(path, MutableStateFlow(false))
            _visibleMap.putIfAbsent(path, true)
        }
    }
}

