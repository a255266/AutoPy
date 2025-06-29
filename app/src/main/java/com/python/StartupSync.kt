package com.python

import dagger.hilt.android.qualifiers.ApplicationContext
import android.content.Context
import android.util.Log
import com.python.data.SyncFileDao
import com.python.data.WebDavManager
import com.python.ui.viewmodels.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StartupSync @Inject constructor(
    private val webDavManager: WebDavManager,
    private val syncFileDao: SyncFileDao,
    @ApplicationContext private val context: Context
) {
    fun sync(onDownload: () -> Unit) {
        CoroutineScope(Dispatchers.Default).launch {
            try {
                webDavManager.incrementalSync(context, syncFileDao)
                Log.d("StartupSync", "✅ 启动同步完成")
                onDownload()
            } catch (e: Exception) {
                Log.e("StartupSync", "❌ 启动同步失败", e)
            }
        }
    }
}

