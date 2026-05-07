package com.likelion.ca.presentation.launcher.viewmodel

/**
 * 런처 화면의 상태를 나타내는 클래스입니다.
 */
sealed interface LauncherUiState {
    object Initial : LauncherUiState
    object Loading : LauncherUiState
    data class Success(val destination: String) : LauncherUiState
    data class Error(val message: String) : LauncherUiState
}
