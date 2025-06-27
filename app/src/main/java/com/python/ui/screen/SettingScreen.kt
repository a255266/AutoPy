package com.python.ui.screen

import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.python.data.WebDavKeys
import com.python.data.WebDavSettings
import com.python.ui.theme.autoPyGradientBackground
import com.python.ui.viewmodels.SettingsViewModel
import com.python.util.throttleClick
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
//import android.content.Context

//设置页面
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    // 刷新通知权限状态
    LaunchedEffect(Unit) {
        viewModel.refreshNotificationPermission(context)
        viewModel.refreshBatteryOptimization(context)
    }


    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshNotificationPermission(context)
    }

    // 电池优化权限设置启动器
    val settingsLauncherBattery = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.refreshBatteryOptimization(context)
    }

    val notificationEnabled by viewModel.notificationEnabled.collectAsState()
    val batteryOptState by viewModel.batteryOptimizedAllowed.collectAsState()



    val settings by viewModel.webDavSettings.collectAsState(
        initial = WebDavSettings("", "", "", "16", "", false, false)
    )

    // Compose 状态变量，绑定输入框
    var server by remember { mutableStateOf(settings.server) }
    var account by remember { mutableStateOf(settings.account) }
    var password by remember { mutableStateOf(settings.password) }
    var passwordLength by remember { mutableStateOf(settings.passwordLength) }
    var decryptKey by remember { mutableStateOf(settings.decryptKey) }
    var foregroundServiceEnabled by remember { mutableStateOf(settings.foregroundServiceEnabled) }
    var allowSystemSettings by remember { mutableStateOf(settings.allowSystemSettings) }


    // 外部数据变化时，同步更新 Compose 状态，避免UI与数据不同步
    LaunchedEffect(settings) {
        if (server != settings.server) server = settings.server
        if (account != settings.account) account = settings.account
        if (password != settings.password) password = settings.password
        if (passwordLength != settings.passwordLength) passwordLength = settings.passwordLength
        if (decryptKey != settings.decryptKey) decryptKey = settings.decryptKey
        if (foregroundServiceEnabled != settings.foregroundServiceEnabled) foregroundServiceEnabled =
            settings.foregroundServiceEnabled
        if (allowSystemSettings != settings.allowSystemSettings) allowSystemSettings =
            settings.allowSystemSettings
    }


    Log.d("NotifCheck", "Compose重组: notificationEnabled=$notificationEnabled")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .autoPyGradientBackground()
    ) {
        Scaffold(
            containerColor = Color.Transparent, // Scaffold背景透明
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("设置") },
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent // TopAppBar背景透明
                    )
                )
            }
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
            ) {
                SectionTitle("云同步")
                SettingsCard {

                    SettingField(
                        label = "WebDav服务器地址",
                        value = server,
                        onValueChange = {
                            server = it
                            viewModel.updateString(WebDavKeys.SERVER, it)
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                    )
                    SettingField(
                        label = "WebDav账号",
                        value = account,
                        onValueChange = {
                            account = it
                            viewModel.updateString(WebDavKeys.ACCOUNT, it)
                        },
                    )
                    SettingField(
                        label = "WebDav密码",
                        value = password,
                        onValueChange = {
                            password = it
                            viewModel.updateString(WebDavKeys.PASSWORD, it)
                        }
                    )
                    TextButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://help.jianguoyun.com/?p=2064")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        shape = RectangleShape
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("坚果云授权教程", fontSize = 20.sp)
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
                        }
                    }
                }
                SectionTitle("权限")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp)
                        .padding(horizontal = 14.dp), // 左右各 16.dp 边距
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "前台服务",
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface

                    )
                    Switch(
                        checked = notificationEnabled,
                        onCheckedChange = {
                            val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                            }
                            settingsLauncher.launch(intent)
                        }
                    )
                }

                //
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "电池优化权限",
                        fontSize = 20.sp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Switch(
                        checked = batteryOptState,
                        onCheckedChange = {
                            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            settingsLauncherBattery.launch(intent)
                        }
                    )
                }

                //修改系统设置
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 7.dp)
                        .padding(horizontal = 14.dp), // 左右各 16.dp 边距
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "修改系统设置",
                        fontSize = 20.sp ,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface

                    )
                    Switch(
                        checked = allowSystemSettings,
                        onCheckedChange = {
                            allowSystemSettings = it
                            viewModel.updateBoolean(WebDavKeys.ALLOW_SYSTEM_SETTINGS, it)
                        }
                    )
                }

//                SectionTitle("参数")
//
////        SectionTitle("配置密码生成器")
//                OutlinedTextField(
//                    value = passwordLength,
//                    onValueChange = {
//                        passwordLength = it
//                        viewModel.updateString(WebDavKeys.PASSWORD_LENGTH, it)
//                    },
//                    label = { Text("密码生成器") },
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(
//                            start = 16.dp,
//                            end = 16.dp,
//                            top = 5.dp,
//                        ),
//                    placeholder = { Text("默认生成16位") },
//                    singleLine = true,
//                    keyboardOptions = KeyboardOptions.Default.copy(
//                        keyboardType = KeyboardType.Number
//                    )
//                )
                Spacer(modifier = Modifier.height(100.dp))


            }
        }
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(title, modifier = Modifier.padding(start = 26.dp, top = 6.dp), color = Color.Gray)
}


@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            content()
        }
    }
}


@Composable
fun SettingField(label: String, value: String, onValueChange: (String) -> Unit,modifier: Modifier = Modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = MaterialTheme.colorScheme.onSurface) },
        modifier = modifier,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}