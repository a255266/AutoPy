package com.python.ui.screen

import android.content.Context
import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.python.data.LogManager
import com.python.ui.theme.autoPyGradientBackground
import androidx.compose.ui.graphics.Color
import com.python.util.throttleClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    context: Context = LocalContext.current,
) {

    Log.d("LogScreen", "LogScreen recomposed")
    val logText by LogManager.logFlow.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        LogManager.loadLog()
    }

    LaunchedEffect(logText) {
        scrollState.animateScrollTo(
            scrollState.maxValue,
            animationSpec = tween(durationMillis = 300)
        )
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .autoPyGradientBackground()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("日志") },
                    navigationIcon = {
                        IconButton(onClick = {
                            throttleClick("back") {
                            navController.popBackStack()
                        } }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        LogManager.clear()
                    },
                    modifier = Modifier
                        .padding(end = 35.dp, bottom = 75.dp)
                        .size(66.dp)
                ) {
                    Icon(Icons.Default.Close, contentDescription = "清除日志")
                }
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(start = 16.dp, end = 16.dp, bottom = 200.dp)
            ) {
                Text(
                    text = logText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


//package com.python.ui.screen
//
//import android.content.Context
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.IconButton
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Scaffold
//import androidx.compose.material3.Text
//import androidx.compose.material3.TopAppBar
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.unit.dp
//import com.python.ui.theme.autoPyGradientBackground
//import java.io.File
//import android.content.res.Configuration
//import androidx.compose.foundation.background
//import androidx.compose.foundation.isSystemInDarkTheme
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.automirrored.filled.ArrowBack
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material.icons.filled.Close
//import androidx.compose.material.icons.filled.DateRange
//import androidx.compose.material.icons.filled.List
//import androidx.compose.material.icons.filled.Search
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.*
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.setValue
//
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.unit.dp
//import androidx.navigation.NavHostController
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.layout.onSizeChanged
//import androidx.compose.ui.tooling.preview.Preview
//import androidx.compose.ui.unit.IntSize
//import androidx.navigation.compose.rememberNavController
//import com.python.ui.theme.AutoPyTheme
//import com.python.ui.theme.autoPyGradientBackground
//
//
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun LogScreen(
//    navController: NavHostController,
//    modifier: Modifier = Modifier,
//    logFileName: String = "python_run_log.txt",
//    context: Context = LocalContext.current,
//    onBackClick: () -> Unit = {}
//) {
//    var logText by remember { mutableStateOf("加载中...") }
//    val logDir = File(context.filesDir, "python_logs")
//    val file = File(logDir, logFileName)
//
//    LaunchedEffect(Unit) {
//        try {
//            val logDir = File(context.filesDir, "python_logs")
//            val file = File(logDir, logFileName)
//            logText = if (file.exists()) file.readText() else ""
//        } catch (e: Exception) {
//            logText = "读取日志失败：${e.message}"
//        }
//    }
//
////    LaunchedEffect(Unit) {
////        val interval = 100L // 每 1 秒刷新一次
////        while (true) {
////            try {
////                logText = if (file.exists()) file.readText() else ""
////            } catch (e: Exception) {
////                logText = "读取日志失败：${e.message}"
////            }
////            kotlinx.coroutines.delay(interval)
////        }
////    }
//
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .autoPyGradientBackground()
//    ) {
//        Scaffold(
//            containerColor = Color.Transparent, // Scaffold背景透明
//            topBar = {
//                TopAppBar(
//                    title = { Text("日志") },
//                    navigationIcon = {
//                        IconButton(onClick = { navController.popBackStack() }) {
//                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
//                        }
//                    },
//                    colors = TopAppBarDefaults.topAppBarColors(
//                        containerColor = Color.Transparent // TopAppBar背景透明
//                    )
//                )
//            },
////            FloatingActionButton(
////                onClick = { showDialog = true },
////                modifier = Modifier
////                    .padding(end = 35.dp, bottom = 75.dp) // 控制边距
////                    .size(66.dp) // 控制按钮尺寸，默认是 56.dp
////            ) {
////                Icon(
////                    imageVector = Icons.Default.Add,
////                    contentDescription = "新增",
////                    modifier = Modifier.size(24.dp) // 控制图标尺寸，默认是 24.dp
////                )
////            }
//            floatingActionButton = {
//                FloatingActionButton(
//                    onClick = {
//                        try {
//                            val logDir = File(context.filesDir, "python_logs")
//                            val file = File(logDir, logFileName)
//                            if (file.exists()) file.writeText("")
//                            logText = ""
//                        } catch (e: Exception) {
//                            logText = "清除日志失败：${e.message}"
//                        }
//                    },
//                    modifier = Modifier
//                        .padding(end = 35.dp, bottom = 75.dp) // 控制边距
//                        .size(66.dp) // 控制按钮尺寸，默认是 56.dp
//
//                ) {
//                    Icon(Icons.Default.Close, contentDescription = "清除日志")
//                }
//            },
////            modifier = modifier
////                .fillMaxSize()
////                .padding(horizontal = 16.dp)
//        ) { padding ->
//            Box(
//                modifier = Modifier
//                    .padding(padding)
//                    .verticalScroll(rememberScrollState())
//                    .padding(start = 16.dp, end = 16.dp, bottom = 200.dp)
//            ) {
//                Text(
//                    text = logText,
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//            }
//        }
//    }
//}