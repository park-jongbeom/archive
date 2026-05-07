package com.likelion.ca.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.presentation.auth.ui.SignScreen
import com.likelion.ca.presentation.chat.ui.ChatRoomListScreen
import com.likelion.ca.presentation.chat.ui.ChatRoomScreen
import com.likelion.ca.presentation.launcher.ui.LauncherScreen
import com.likelion.ca.presentation.setting.ui.SettingScreen

/**
 * 앱 전역 화면 라우팅을 구성하는 네비게이션 그래프입니다.
 *
 * [userRepository]의 인증 상태를 구독해, 로그아웃·세션 만료 시 보호 화면에서 로그인 화면으로 보냅니다.
 */
@Composable
fun ChatAppNavigation(
    navController: NavHostController,
    userRepository: UserRepository,
) {
    val authSession by userRepository.authSession.collectAsState()

    LaunchedEffect(authSession) {
        if (authSession != null) return@LaunchedEffect
        val route = navController.currentBackStackEntry?.destination?.route
        val isProtected = route != null &&
            route != Screen.LauncherScreen.route &&
            route != Screen.SignScreen.route
        if (isProtected) {
            navController.navigateToSignClearingStack()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.LauncherScreen.route,
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
