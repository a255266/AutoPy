package com.python.ui.screen

import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.navigation.compose.rememberNavController
import com.python.ui.theme.AutoPyTheme
import com.python.ui.theme.autoPyGradientBackground
import android.content.Context
import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import java.io.File
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.python.PythonRunner
import com.python.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import java.util.Calendar
import com.python.service.ForegroundService
import android.provider.Settings
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.hilt.navigation.compose.hiltViewModel
import com.python.data.ScheduledTask
import com.python.ui.viewmodels.CloudSyncViewModel
import com.python.ui.viewmodels.HomeViewModel
import com.python.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay


@Composable
fun ObserveFabVisible(
    listState: LazyListState,
    thresholdPx: Int = 30,
    onVisibilityChange: (Boolean) -> Unit
) {
    val density = LocalDensity.current
    val threshold = with(density) { thresholdPx.dp.toPx() }

    var previousScrollOffset by remember { mutableStateOf(0f) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemScrollOffset + listState.firstVisibleItemIndex * 500 }
            .collect { currentOffset ->
                val delta = currentOffset - previousScrollOffset
                previousScrollOffset = currentOffset.toFloat()

                if (delta > threshold) {
                    onVisibilityChange(false)
                } else if (delta < -threshold) {
                    onVisibilityChange(true)
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController = rememberNavController(),
    onSearchClick: () -> Unit = {},
    viewModel: HomeViewModel,
) {



    val homeListState = rememberLazyListState()
    val timingListState = rememberLazyListState()

    val tasks by viewModel.scheduledTasks.collectAsState()

    val context = LocalContext.current
    val tabs = listOf("首页", "定时")
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

//    val fileList by viewModel.fileList.collectAsState()


    var showDialog by remember { mutableStateOf(false) }
    var filename by remember { mutableStateOf("") }
    var size by remember { mutableStateOf(IntSize.Zero) }

    //滑动隐藏悬浮按钮
    var isFabVisible by remember { mutableStateOf(true) }
    ObserveFabVisible(listState = homeListState) { visible ->
        Log.d("AddButton", "FAB 可见性变化: $visible")
        isFabVisible = visible
    }

    // 更新任务函数，直接调用 ViewModel 的更新
    fun updateTask(task: ScheduledTask) {
        viewModel.updateTask(task)
    }





    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { size = it }
            .autoPyGradientBackground()
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior, // ✅ 加上它！
                    modifier = Modifier
                        .fillMaxWidth(),
//                        .height(90.dp),
                    title = {
                        Text(
                            text = "AutoPy",
                            style = MaterialTheme.typography.titleMedium.copy(fontSize = 30.sp)  // 修改文字大小
                        )
                            },
                    actions = {
//                        IconButton(onClick = onSearchClick) {
//                            Icon(Icons.Default.Search, contentDescription = "搜索")
//                        }
                        IconButton(onClick = { navController.navigate("log") }) {
                            Icon(Icons.Default.DateRange, contentDescription = "日志")
                        }
                        IconButton(onClick = { navController.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "设置")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent,
                    ),
                )
            },
            floatingActionButton = {
                if (pagerState.currentPage == 0) {
                    AddButton(
                        isExpand = isFabVisible,
                        onClick = { showDialog = true },
                        modifier = Modifier
                            .padding(end = 35.dp, bottom = 75.dp)
                            .size(66.dp)
                    )
                }
            },
        ) { innerPadding  ->

            Column() {
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxWidth()
                            .padding(horizontal = 120.dp)
                            .height(32.dp), // 控制高度

                        containerColor = Color.Transparent,   // 背景透明
                        contentColor = MaterialTheme.colorScheme.primary,  // 选中颜色
                        indicator = { tabPositions ->
                            // 自定义指示器样式，例如颜色和高度
                            SecondaryIndicator(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                    .offset(y = (-10).dp)
                                    .padding(horizontal = 20.dp)
                                    .clip(RoundedCornerShape(50)),
                                height = 3.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontSize = 16.sp,
                                            color = if (pagerState.currentPage == index)
                                                MaterialTheme.colorScheme.primary
                                            else
                                                Color.Gray

                                        )  // 修改文字大小
                                    )
                                }
                            )
                        }
                    }
//                }
                // 内容区：左右滑动的 Pager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
//                        .fillMaxWidth()
                        .weight(1f) // 关键：避免撑满高度，防止nestedScroll计算负值
                ) { page ->
                    when (page) {
                        0 ->
                            HomeContent(
                            listState = homeListState,
//                            fileList = fileList,
                            onItemClick = { file ->
                                navController.navigate("editor/${file.nameWithoutExtension}")
                            },
                            onItemLongClick = { file ->
                                // 删除文件后刷新 fileList
                                if (file.exists()) file.delete()
                                viewModel.deleteFile(file, context)
                            },
                        )

                        1 -> TimingContent(
                            listState = timingListState,
                            taskList = tasks,
                            onUpdateTask = { updatedTask -> updateTask(updatedTask) },
                            onItemLongClick = { viewModel.deleteTask(it) }
                        )
                    }
                }


            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("新建文件") },
                    text = {
                        OutlinedTextField(
                            value = filename,
                            onValueChange = { filename = it },
                            label = { Text("文件名") },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (filename.isNotBlank()) {
                                val newFileName = filename  // 缓存
                                viewModel.createFile(context, newFileName)
                                filename = ""               // 再清空
                                showDialog = false
                                navController.navigate("editor/$newFileName")  // 使用缓存值
                            }
                        }) {
                            Text("确定")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("取消")
                        }
                    },
                )
            }
        }
    }
}


//TODO首页
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    listState: LazyListState,
//    fileList: List<File>,
    onItemClick: (File) -> Unit,
    onItemLongClick: (File) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    cloudSyncViewModel: CloudSyncViewModel = hiltViewModel()
) {

    val fileList by viewModel.fileList.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFiles()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()


//    LaunchedEffect(entries) {
//        Log.d("WebDAV", "entries changed: $entries")
//    }





    // 用 entries 触发刷新，返回排序后的文件列表
    val sortedFileList = remember(fileList) {
        Log.d("WebDAV", "fileList changed: $fileList")
        Log.d("WebDAV", "sortedFileList recomputed due to entries or fileList change")
        fileList.sortedByDescending { it.lastModified() }
    }
    Log.d("WebDAV", "fileList size: ${fileList.size}")
    Log.d("WebDAV", "sortedFileList size: ${sortedFileList.size}")

    // 这里用 remember 保持状态，避免重复初始化
    val runningMap = remember { mutableStateMapOf<String, Boolean>() }
    val visibleMap = remember { mutableStateMapOf<String, Boolean>() }

    // 确保 runningMap 和 visibleMap 包含所有文件路径的默认值
    LaunchedEffect(sortedFileList) {
        Log.d("WebDAV", sortedFileList.toString())
        sortedFileList.forEach { file ->
            val path = file.absolutePath
            if (!runningMap.containsKey(path)) runningMap[path] = false
            if (!visibleMap.containsKey(path)) visibleMap[path] = true
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    Log.d("WebDAV", "HomeScreen recomposed")
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            coroutineScope.launch {
                isRefreshing = true
                try {
                    cloudSyncViewModel.syncNow(context) {
                        viewModel.loadFiles()
                        Log.d("WebDAV", "同步完成，准备结束刷新")
//                        isRefreshing = false  // ✅ 确保结束刷新动画
                    }
                } catch (e: Exception) {
                    Log.e("WebDAV", "❌ 下拉同步异常", e)
                } finally {
                    delay(1000)
                    Log.d("WebDAV", "刷新结束，isRefreshing 设 false")
                    isRefreshing = false  // ✅ 确保结束刷新动画
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 30.dp)
        ) {
            items(sortedFileList, key = { it.absolutePath }) { file ->


                var showConfirm by remember { mutableStateOf(false) }
                val isRunning = runningMap[file.absolutePath] ?: false
                var showTimePicker by remember { mutableStateOf(false) }

                if (showTimePicker) {
                    TimePickerDialogMaterial3(
                        onDismiss = { showTimePicker = false },
                        onConfirm = { hour, minute, repeatDaily ->
                            showTimePicker = false

                            // 保存到数据库
                            val task = ScheduledTask(
                                filePath = file.absolutePath,
                                hour = hour,
                                minute = minute,
                                repeatDaily = repeatDaily
                            )
                            viewModel.insertTask(task)
                        }
                    )
                }


                val rotation by animateFloatAsState(
                    targetValue = if (isRunning) 360f else 0f,
                    animationSpec = tween(durationMillis = 600)
                )

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(targetValue = if (isPressed) 0.95f else 1.0f)

                val visible = visibleMap[file.absolutePath] ?: true

                if (showConfirm) {
                    AlertDialog(
                        onDismissRequest = { showConfirm = false },
                        title = { Text("删除文件") },
                        text = { Text("确认删除 ${file.name} 吗？") },
                        confirmButton = {
                            TextButton(onClick = {
                                // 先播放淡出动画
                                visibleMap[file.absolutePath] = false
                                showConfirm = false
                                // 延迟动画时长后执行删除操作
                                coroutineScope.launch {
                                    delay(1000) // 动画时长
                                    onItemLongClick(file)
                                }
                            }) {
                                Text("删除")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirm = false }) {
                                Text("取消")
                            }
                        }
                    )
                }

                AnimatedVisibility(
                    visible = visible,
                    exit = fadeOut(tween(500)) + scaleOut(tween(500), targetScale = 1.5f),
                    modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
                ) {
                    Column {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .graphicsLayer(scaleX = scale, scaleY = scale)
                                .combinedClickable(
                                    interactionSource = interactionSource,
                                    indication = null,
                                    onClick = { onItemClick(file) },
                                    onLongClick = { showConfirm = true }
                                )
                                .animateContentSize(),
                            shape = RoundedCornerShape(12.dp),
//                        shape = RectangleShape,
//                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp, end = 8.dp, top = 6.dp, bottom = 6.dp)
                            ) {
                                Text(
                                    text = file.name,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        if (!isRunning) {
                                            runningMap[file.absolutePath] = true
                                            coroutineScope.launch {
                                                withContext(Dispatchers.IO) {
                                                    PythonRunner.runFileWithRunpy(context, file)
                                                }
                                                runningMap[file.absolutePath] = false
                                            }

                                        } else {
                                            // 目前不支持暂停，点击运行中按钮不做事
                                        }
                                    }
                                ) {
                                    Icon(
                                        painter = if (isRunning)
                                            painterResource(id = R.drawable.runcentre) // 你运行中的图标
                                        else
                                            painterResource(id = R.drawable.run),
                                        contentDescription = if (isRunning) "运行中" else "运行",
                                        tint = Color.Unspecified,
                                        modifier = Modifier
                                            .size(33.dp)
                                            .graphicsLayer(rotationZ = rotation)
                                    )
                                }
                                IconButton(onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                        val alarmManager =
                                            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                        if (!alarmManager.canScheduleExactAlarms()) {
                                            val intent =
                                                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                                            context.startActivity(intent)
                                            return@IconButton
                                        }
                                    }

                                    showTimePicker = true
                                }
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.timing1),
                                        contentDescription = "定时",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}


//TODO定时页面
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimingContent(
//    modifier: Modifier = Modifier,
    listState: LazyListState,
    taskList: List<ScheduledTask>,
    onUpdateTask: (ScheduledTask) -> Unit,
    onItemLongClick: (ScheduledTask) -> Unit,
) {



    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope() // 创建协程作用域
    val visibleMap = remember { mutableStateMapOf<Int, Boolean>() }
    var showTimePicker by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<ScheduledTask?>(null) }
    taskList.forEach { task ->
        visibleMap.putIfAbsent(task.id, true)
    }

    fun cancelScheduledTask(context: Context, task: ScheduledTask) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ForegroundService::class.java).apply {
            putExtra("filePath", task.filePath)
        }

        val pendingIntent = PendingIntent.getForegroundService(
            context,
            task.id, // 必须与 schedule 中用的 requestCode 相同
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.cancel(pendingIntent)
    }


    if (showTimePicker && editingTask != null) {
        TimePickerDialogMaterial3(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute, repeatDaily ->
                showTimePicker = false
                val updated = editingTask!!.copy(hour = hour, minute = minute, repeatDaily = repeatDaily)
                onUpdateTask(updated)  // 调用更新数据库的方法
            }
        )
    }



    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 30.dp)
    ) {
        items(taskList, key = { it.id }) { task ->

            var showConfirm by remember { mutableStateOf(false) }
            val visible = visibleMap[task.id] ?: true
            if (showConfirm) {
                AlertDialog(
                    onDismissRequest = { showConfirm = false },
                    title = { Text("删除定时任务") },
                    text = { Text("确定删除 ${File(task.filePath).name} 的任务吗？") },
                    confirmButton = {
                        TextButton(onClick = {
                            visibleMap[task.id] = false
                            showConfirm = false
                            coroutineScope.launch {
                                delay(500)
                                cancelScheduledTask(context, task)
                                onItemLongClick(task)
                            }
                        }) { Text("删除") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirm = false }) { Text("取消") }
                    }
                )
            }

            AnimatedVisibility(
                visible = visible,
                exit = fadeOut(tween(500)) + scaleOut(tween(500), targetScale = 1.5f),
                modifier = Modifier.animateItem(fadeInSpec = null, fadeOutSpec = null)
            ) {
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .clip(RoundedCornerShape(10.dp)) // 裁剪内容以适配圆角
                            .animateContentSize()
                            .combinedClickable(
                                onClick = {
                                    editingTask = task
                                    showTimePicker = true
                                },
                                onLongClick = { showConfirm = true }
                            ),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    start = 12.dp,
                                    end = 12.dp,
                                    top = 17.dp,
                                    bottom = 17.dp
                                )
                        ) {
                            Text(
                                text = File(task.filePath).name,
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                            )
                            Text(
                                text = "定时：${
                                    task.hour.toString().padStart(2, '0')
                                }:${
                                    task.minute.toString().padStart(2, '0')
                                } ${if (task.repeatDaily) "每日执行" else "执行一次"}",
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp),
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun AddButton(isExpand: Boolean, modifier: Modifier = Modifier,onClick: () -> Unit) {
    Log.d("AddButton", "isExpand = $isExpand")
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isExpand,
            modifier = Modifier.align(Alignment.BottomEnd),
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            FloatingActionButton(
                onClick = onClick,
                modifier = modifier,
//                elevation = FloatingActionButtonDefaults.elevation(3.dp) // 阴影
//                shape = CircleShape,
            ) {
                Icon(
                    Icons.Filled.Add, "新增"
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialogMaterial3(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int, repeatDaily: Boolean) -> Unit
) {
    val timePickerState = rememberTimePickerState(initialHour = 9, initialMinute = 0)
    var repeatDaily by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置定时任务") },
        text = {
            Column {
                TimePicker(state = timePickerState)
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = repeatDaily, onCheckedChange = { repeatDaily = it })
                    Spacer(Modifier.width(8.dp))
                    Text("每日重复")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute, repeatDaily)
                onDismiss()
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}



fun schedulePythonExecution(context: Context, file: File, hour: Int, minute: Int, repeatDaily: Boolean) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, ForegroundService::class.java).apply {
        putExtra("filePath", file.absolutePath)
    }

    val pendingIntent = PendingIntent.getForegroundService(
        context, file.hashCode(), intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
    }

    if (repeatDaily) {
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    } else {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
}





//@Preview(name = "Light Theme", showBackground = true)
//@Composable
//fun HomeScreenLightPreview() {
//    AutoPyTheme(darkTheme = false) {
//        HomeScreen()
//    }
//}
//
//@Preview(name = "Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
//@Composable
//fun HomeScreenDarkPreview() {
//    AutoPyTheme(darkTheme = true) {
//        HomeScreen()
//    }
//}
