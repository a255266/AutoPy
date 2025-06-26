package com.python

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.python.ui.screen.*

@Composable
fun AppNav(navController: NavHostController) {
    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController = navController)  // ✅ 使用整合后的页面
        }
        composable("editor/{filename}") { backStackEntry ->
            val filename = backStackEntry.arguments?.getString("filename") ?: "未命名"
            EditorScreen(navController, filename)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("log") {
            LogScreen(navController = navController)
        }
    }
}