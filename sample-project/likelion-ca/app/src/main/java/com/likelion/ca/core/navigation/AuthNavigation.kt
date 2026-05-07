package com.likelion.ca.core.navigation

import androidx.navigation.NavController

/**
 * 로그아웃·세션 만료 등으로 로그인 화면으로 이동할 때 백스택 전체를 비웁니다.
 */
fun NavController.navigateToSignClearingStack() {
    navigate(Screen.SignScreen.route) {
        popUpTo(graph.id) {
            inclusive = true
        }
        launchSingleTop = true
    }
}
