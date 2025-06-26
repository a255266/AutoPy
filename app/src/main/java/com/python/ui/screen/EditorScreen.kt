package com.python.ui.screen

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.TextButton
import com.python.ui.theme.autoPyGradientBackground
import androidx.compose.ui.platform.LocalContext
import com.python.PythonRunner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import com.python.R
import com.python.util.throttleClick
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun EditorScreen(
    navController: NavHostController,
    filename: String,
    onSearchClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onLogClick: () -> Unit = {},
) {
    val context = LocalContext.current

    // 先确定文件夹路径和文件对象
    val file = remember {
        File(context.filesDir, "python_files/$filename.py").apply { if (!exists()) createNewFile() }
    }

    var codeText by remember { mutableStateOf(TextFieldValue("")) }

    // 读取文件内容，只在第一次进入时加载
    LaunchedEffect(file) {
        try {
            codeText = TextFieldValue(file.readText())
        } catch (e: Exception) {
            Log.e("EditorScreen", "读取文件失败", e)
        }
    }

    // 节流保存逻辑，避免频繁写入磁盘
    LaunchedEffect(codeText.text) {
        snapshotFlow { codeText.text }
            .debounce(1000)
            .collectLatest { text ->
                try {
                    file.writeText(text)
                } catch (e: Exception) {
                    Log.e("EditorScreen", "写入文件失败", e)
                }
            }
    }

    val coroutineScope = rememberCoroutineScope() // 创建协程作用域
    var isRunning by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isRunning) 360f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "rotation"
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
            .autoPyGradientBackground()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = filename,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        ) },
                    navigationIcon = {
                        IconButton(onClick = {
                            throttleClick("back") {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "返回"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (!isRunning) {
                                    isRunning = true
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            PythonRunner.run(context, codeText.text, filename)
                                        }
                                        isRunning = false
                                    }
                                }
                            }
                        ) {
                            Icon(
                                painter = if (isRunning)
                                    painterResource(id = R.drawable.runcentre)
                                else
                                    painterResource(id = R.drawable.run),
                                contentDescription = if (isRunning) "运行中" else "运行",
                                tint = Color.Unspecified,
                                modifier = Modifier
                                    .size(33.dp)
                                    .graphicsLayer(rotationZ = rotation)
                            )
                        }
                        IconButton(onClick = { navController.navigate("log") }) {
                            Icon(Icons.Default.DateRange, contentDescription = "日志")
                        }
                        IconButton(onClick = onSettingsClick) {
                            Icon(Icons.Default.Done, contentDescription = "保存")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            CodeEditorWithLineNumbers(
                modifier = Modifier
                    .padding(padding)
                    .imePadding(),
                text = codeText,
                onTextChange = { codeText = it }
            )
        }
    }
}





@Composable
fun CodeEditorWithLineNumbers(
    modifier: Modifier = Modifier,
    text: TextFieldValue,
    onTextChange: (TextFieldValue) -> Unit,
) {
    val lines = text.text.split('\n')

    val textStyle = TextStyle(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontFamily = FontFamily.Monospace,
        // 禁止换行（不会自动换行）
        textAlign = TextAlign.Start
    )

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val focusRequester = remember { FocusRequester() }

    // 检测输入法是否弹出
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    var wasImeVisible by remember { mutableStateOf(false) }

//     当输入法关闭时清除焦点
    LaunchedEffect(imeVisible) {
        if (wasImeVisible && !imeVisible) {
            focusManager.clearFocus()
        }
        wasImeVisible = imeVisible
    }

//    LaunchedEffect(Unit) {
//        Log.d("EditorDebug", "LaunchedEffect running")
//        focusRequester.requestFocus()
//        keyboardController?.show()
//    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(verticalScroll)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
            .focusable()
    ) {
        // 行号列
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.Top
        ) {
            lines.forEachIndexed { index, _ ->
                Text(
                    text = "${index + 1}",
                    style = textStyle,
                    color = Color(0xFF018786),
                    modifier = Modifier.height(20.sp.toDp())
                )
            }
        }

//         代码编辑区，带水平滚动
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .horizontalScroll(horizontalScroll) // 水平滚动
                .padding(end = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
        ) {
            BasicTextField(
                value = text,
                onValueChange = onTextChange,
                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
                textStyle = textStyle,
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth()

//                value = text,
//                onValueChange = { text = it },
////                enabled = false,
//                cursorBrush = SolidColor(MaterialTheme.colorScheme.onSurface),
//                textStyle = textStyle,
//                modifier = Modifier
//                    .focusRequester(focusRequester)
//                    .fillMaxWidth()
//////                    .height(810.dp),
            )
        }
    }
}






@Composable
fun TextUnit.toDp(): Dp {
    return with(LocalDensity.current) { this@toDp.toDp() }
}
