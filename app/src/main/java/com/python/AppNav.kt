package com.python

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.python.ui.screen.*
import com.python.ui.viewmodels.CloudSyncViewModel
import com.python.ui.viewmodels.HomeViewModel

@Composable
fun AppNav(navController: NavHostController) {
    val homeViewModel: HomeViewModel = hiltViewModel()
    val cloudSyncViewModel: CloudSyncViewModel = hiltViewModel()
    NavHost(navController, startDestination = "home") {

        composable("home") {
            HomeScreen(
                navController = navController,
                viewModel = homeViewModel
            )
        }
        composable("editor/{filename}") { backStackEntry ->
            val filename = backStackEntry.arguments?.getString("filename") ?: "未命名"
            EditorScreen(
                navController = navController,
                filename = filename,
                viewModel = cloudSyncViewModel)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("log") {
            LogScreen(navController = navController)
        }
    }
}