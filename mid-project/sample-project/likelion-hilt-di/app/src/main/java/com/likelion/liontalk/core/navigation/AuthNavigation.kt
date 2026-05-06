package com.likelion.liontalk.core.navigation

import androidx.navigation.NavController
import com.likelion.liontalk.core.data.repository.UserRepository

/**
 * 로그아웃·세션 만료 등으로 로그인 화면으로 이동할 때 백스택 전체를 비웁니다.
 * (이전 화면으로 돌아가면 비로그인 상태에서 보호 화면이 남지 않도록 함)
 */
fun NavController.navigateToSignClearingStack() {
    navigate(Screen.SignScreen.route) {
        popUpTo(graph.id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

/**
 * 현재 Firebase 인증·프로필 상태에 맞는 시작(또는 복귀) 목적지 route를 계산합니다.
 *
 * @param provider 로그인 직후 등 `ensureUserProfile`에 넘길 provider(선택).
 */
suspend fun UserRepository.computeStartDestination(provider: String? = null): String {
    val firebaseUser = authUser.value
    if (firebaseUser == null) return Screen.SignScreen.route
    ensureUserProfile(provider = provider)
    val user = meOrNull
    return if (user == null || user.name.isBlank()) {
        Screen.SettingScreen.route
    } else {
        Screen.ChatRoomListScreen.route
    }
}
