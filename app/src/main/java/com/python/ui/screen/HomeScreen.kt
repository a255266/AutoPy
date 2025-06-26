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
        snapshotFlow { listState.firstVisibleItemScrollOffset + listState.firstVisibleItemIndex * 1000 }
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
    onSettingsClick: () -> Unit = {},
    onLogClick: () -> Unit = {},
) {


    val context = LocalContext.current
    val tabs = listOf("首页", "定时")
    val pagerState = rememberPagerState { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var fileList by remember { mutableStateOf<List<File>>(emptyList()) }
    var runResult by remember { mutableStateOf<String?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var filename by remember { mutableStateOf("") }
    var size by remember { mutableStateOf(IntSize.Zero) }

    val baseDir = File(context.filesDir, "python_files")

    //滑动隐藏悬浮按钮
    var isFabVisible by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()
    ObserveFabVisible(listState = listState) { isFabVisible = it }




    LaunchedEffect(Unit) {
        if (!baseDir.exists()) baseDir.mkdirs()
        fileList = baseDir.listFiles()?.filter { it.isFile } ?: emptyList()
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
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
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
//                        navigationIconContentColor = MaterialTheme.colorScheme.primary,
//                        titleContentColor = MaterialTheme.colorScheme.primary,
//                        actionIconContentColor = MaterialTheme.colorScheme.primary,
                    ),
                )
            },
            floatingActionButton = {
                AddButton(
                    isExpand = isFabVisible,
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .padding(end = 35.dp, bottom = 75.dp) // 控制边距
                        .size(66.dp) // 控制按钮尺寸，默认是 56.dp
                )
//                AddButton(
//                    isExpand = isFabVisible,
//                    onClick = { showDialog = true },
//                    modifier = Modifier
//                        .padding(end = 35.dp, bottom = 75.dp) // 控制边距
//                        .size(66.dp) // 控制按钮尺寸，默认是 56.dp
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Add,
//                        contentDescription = "新增",
//                        modifier = Modifier.size(24.dp) // 控制图标尺寸，默认是 24.dp
//                    )
//                }
            },
        ) { innerPadding  ->


            Column(
//                modifier = Modifier
//                    .padding(innerPadding)
//                    .fillMaxSize()
            ) {
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
//                        0 -> HomeContent(
//                            modifier = Modifier
//                                .padding(innerPadding),
//                            fileList = fileList,
//                            onRunClick = { file ->
//                                runResult = PythonRunner.runFileWithRunpy(context, file)
//                                showResultDialog = true
//                            },
//                            onItemClick = { file ->
//                                navController.navigate("editor/${file.nameWithoutExtension}")
//                            },
//                            onItemLongClick = { file ->
//                                // 删除文件并刷新列表
//                                // 删除文件后刷新 fileList
//                                if (file.exists()) file.delete()
//                                val files = baseDir.listFiles()?.filter { it.isFile } ?: emptyList()
//                                fileList = files
//                            },
//                            runResult = runResult,
//                            showResultDialog = showResultDialog,
//                            onDismissDialog = { showResultDialog = false }
//                        )
//                        0 ->    LazyColumn(
//                            modifier = Modifier
//                                .padding(innerPadding)
//                                .nestedScroll(scrollBehavior.nestedScrollConnection)
//                                .fillMaxSize(),
//                            contentPadding = PaddingValues(16.dp),
//                            verticalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            items(30) { index ->
//                                Card(
//                                    modifier = Modifier.fillMaxWidth(),
//                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//                                ) {
//                                    Text(
//                                        "Item #$index",
//                                        modifier = Modifier.padding(16.dp),
//                                        style = MaterialTheme.typography.bodyLarge
//                                    )
//                                }
//                            }
//                        }


                        0 -> HomeContent(
                            listState = listState,
                            modifier = Modifier
//                                .padding(innerPadding)
                            ,
                            fileList = fileList,
                            onRunClick = { file ->
                                runResult = PythonRunner.runFileWithRunpy(context, file)
                                showResultDialog = true
                            },
                            onItemClick = { file ->
                                navController.navigate("editor/${file.nameWithoutExtension}")
                            },
                            onItemLongClick = { file ->
                                // 删除文件并刷新列表
                                // 删除文件后刷新 fileList
                                if (file.exists()) file.delete()
                                val files = baseDir.listFiles()?.filter { it.isFile } ?: emptyList()
                                fileList = files
                            },
                            runResult = runResult,
                            showResultDialog = showResultDialog,
                            onDismissDialog = { showResultDialog = false }
                        )
//
                        1 -> TimingContent() // 你的定时页面
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
                                val newFileName = filename
                                createPyFile(context, newFileName)
                                filename = ""  // 清空输入框
                                showDialog = false
                                // 刷新文件列表
                                val dir = File(context.filesDir, "python_files")
                                val files = dir.listFiles()?.filter { it.isFile }?.toList() ?: emptyList()
                                fileList = files

                                navController.navigate("editor/$newFileName")  // 传入保存的变量
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

fun createPyFile(context: Context, filename: String) {
    val codeDir = File(context.filesDir, "python_files")
    if (!codeDir.exists()) {
        codeDir.mkdirs()
    }
    val file = File(codeDir, "$filename.py")
    if (!file.exists()) {
        file.writeText("")  // 创建空文件
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    fileList: List<File>,
    onRunClick: (File) -> Unit,
    onItemClick: (File) -> Unit,
    onItemLongClick: (File) -> Unit,
    runResult: String?,
    showResultDialog: Boolean,
    onDismissDialog: () -> Unit
) {


    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope() // 创建协程作用域

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val runningMap = remember { mutableStateMapOf<String, Boolean>() }
    // 管理每个文件的显示状态，初始都为 true
    val visibleMap = remember { mutableStateMapOf<String, Boolean>() }




    fileList.forEach { file ->
        val path = file.absolutePath
        runningMap.putIfAbsent(path, false)
        visibleMap.putIfAbsent(path, true)
    }


    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 30.dp)
    ) {
        items(fileList, key = { it.absolutePath }) { file ->
            var showConfirm by remember { mutableStateOf(false) }
            val isRunning = runningMap[file.absolutePath] ?: false

            val rotation by animateFloatAsState(
                targetValue = if (isRunning) 360f else 0f,
                animationSpec = tween(durationMillis = 600)
            )

            val interactionSource = remember { MutableInteractionSource() }
            val isPressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f)

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
                                kotlinx.coroutines.delay(1000) // 动画时长
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
                            .layout { measurable, constraints ->
                                Log.d("LayoutDebug", "Measured with: $constraints")
                                val placeable = measurable.measure(constraints)
                                layout(placeable.width, placeable.height) {
                                    placeable.place(0, 0)
                                }
                            }
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
                        Column(modifier = Modifier.padding(start = 12.dp,end = 8.dp,  top = 6.dp, bottom = 6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
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
                                IconButton(onClick = {  }) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.timing1),
                                        contentDescription = "定时",
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(25.dp)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            }
        }
    }
//}

@Composable
fun AddButton(isExpand: Boolean, modifier: Modifier = Modifier,onClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = isExpand,
            modifier = Modifier.align(Alignment.BottomEnd),
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
//            enter = scaleIn(initialScale = 0.1f),
//            exit = scaleOut(targetScale = 0.1f)
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

//@Composable
//fun HomeContent() {
//    // 首页内容，比如列表
//    Text("这里是首页内容")
//}

//定时页面
@Composable
fun TimingContent() {
    Text("这里是定时内容")
}



@Preview(name = "Light Theme", showBackground = true)
@Composable
fun HomeScreenLightPreview() {
    AutoPyTheme(darkTheme = false) {
        HomeScreen()
    }
}

@Preview(name = "Dark Theme", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    AutoPyTheme(darkTheme = true) {
        HomeScreen()
    }
}
