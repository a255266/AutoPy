package com.python

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.python.ui.theme.AutoPyTheme
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import android.Manifest
import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.python.data.LogManager
import com.python.service.ForegroundService
import com.python.ui.viewmodels.HomeViewModel
import com.python.ui.viewmodels.SettingsViewModel
import javax.inject.Inject


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var startupSync: StartupSync
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 初始化 Chaquopy
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val intent = Intent(this, ForegroundService::class.java)
        ContextCompat.startForegroundService(this, intent)

        val homeViewModel: HomeViewModel by viewModels()

        // 1️⃣ 先触发同步
        startupSync.sync(
            onDownload = {
                Log.d("MainActivity", "📥 下载触发，刷新文件列表")
                homeViewModel.loadFiles()
            }
        )

        enableEdgeToEdge()
        setContent {
            AutoPyTheme {
                val navController = rememberNavController()
                RequestNotificationPermission()
                // 整个 App UI 容器
                AppContent(navController)
            }
        }
    }
}

@Composable
fun AppContent(navController: androidx.navigation.NavHostController) {
        AppNav(navController)  // 直接显示导航内容
}

@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val context = LocalContext.current
        val permissionState = remember {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                permissionState.value = granted
            }
        )

        LaunchedEffect(Unit) {
            if (!permissionState.value) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

