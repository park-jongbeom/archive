package com.likelion.liontalk.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.likelion.liontalk.features.chat.ui.ChatRoomScreen
import com.likelion.liontalk.features.chat.ui.ChatRoomListScreen
import com.likelion.liontalk.features.launcher.ui.LauncherScreen
import com.likelion.liontalk.features.setting.ui.SettingScreen
import com.likelion.liontalk.features.auth.ui.SignScreen

/**
 * 앱 전역 화면 라우팅을 구성하는 네비게이션 그래프입니다.
 */
@Composable
fun ChatAppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.LauncherScreen.route
    ) {
        composable(Screen.ChatRoomListScreen.route) {
            ChatRoomListScreen(navController)
        }

        composable(Screen.ChatRoomScreen.route) { backStackentry ->
            val roomId = backStackentry.arguments?.getString("roomId")?.toString()
            if (roomId != null) {
                ChatRoomScreen(navController, roomId)
            }
        }

        composable(Screen.SettingScreen.route) {
            SettingScreen(navController)
        }

        composable(Screen.LauncherScreen.route) {
            LauncherScreen(navController)
        }

        composable(Screen.SignScreen.route) {
            SignScreen(navController)
        }
    }
}
