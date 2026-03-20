package com.likelion.liontalk.features.launcher.viewmodel

import androidx.lifecycle.ViewModel
import com.likelion.liontalk.core.data.auth.AuthStateRepository
import com.likelion.liontalk.core.navigation.Screen
import com.likelion.liontalk.core.ui.error.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
/**
 * 앱 시작 시 인증 상태를 확인하고 최초 라우팅 목적지를 결정하는 ViewModel입니다.
 */
class LauncherViewModel @Inject constructor(
    private val authStateRepository: AuthStateRepository
) : ViewModel() {
    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    /** UI에서 처리할 일회성 이벤트 스트림입니다. */
    val uiEvents = _uiEvents.asSharedFlow()

    /**
     * 현재 인증 상태를 바탕으로 시작 라우팅 목적지를 계산합니다.
     * 실패 시 다이얼로그 이벤트를 발생시키고 `null`을 반환합니다.
     */
    suspend fun decideStartDestination(): String? {
        return try {
            val authUser = authStateRepository.authUser.value
            if (authUser == null) {
                Screen.SignScreen.route
            } else {
                authStateRepository.ensureUserProfile()
                val user = authStateRepository.meOrNull
                if (user == null || user.name.isBlank()) {
                    Screen.SettingScreen.route
                } else {
                    Screen.ChatRoomListScreen.route
                }
            }
        } catch (e: Exception) {
            val msg = e.message?.trim()?.takeIf { it.isNotBlank() } ?: "로그인 정보를 불러오지 못했어요."
            _uiEvents.emit(UiEvent.ShowDialog(message = msg))
            null
        }
    }
}

