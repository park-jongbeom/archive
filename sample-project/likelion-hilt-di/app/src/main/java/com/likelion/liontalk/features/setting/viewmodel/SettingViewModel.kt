package com.likelion.liontalk.features.setting.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.likelion.liontalk.core.data.model.ChatUser
import com.likelion.liontalk.core.data.repository.UserRepository
import com.likelion.liontalk.core.navigation.Screen
import com.likelion.liontalk.core.ui.error.UiEvent
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@HiltViewModel
/**
 * 사용자 프로필 편집/저장/로그아웃을 처리하는 ViewModel입니다.
 */
class SettingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    /** 입력 중인 사용자 이름입니다. */
    var userName by mutableStateOf("")
    /** 입력 중인 아바타 이미지 URL입니다. */
    var avatarUrl by mutableStateOf("")

    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    /** 화면에서 구독하는 일회성 UI 이벤트 스트림입니다. */
    val uiEvents = _uiEvents.asSharedFlow()

    /** 현재 로그인된 사용자의 프로필입니다. */
    val me: ChatUser? get() = userRepository.meOrNull

    private fun Throwable.toUserMessage(default: String): String {
        val msg = message?.trim()
        return msg?.takeIf { it.isNotBlank() } ?: default
    }

    init {
        viewModelScope.launch {
            loadProfile()
        }
    }

    /**
     * 현재 입력된 프로필 정보를 Firestore에 저장합니다.
     */
    fun saveProfile() {
        viewModelScope.launch {
            try {
                val current = me
                if (current != null) {
                    userRepository.updateProfile(
                        uid = current.id,
                        name = userName,
                        avatarUrl = avatarUrl
                    )
                    userRepository.ensureUserProfile()
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("저장에 실패했습니다.")))
            }
        }
    }

    /**
     * 현재 `me` 값을 기반으로 화면 입력값을 채웁니다.
     */
    fun loadProfile() {
        val user = me
        if(user != null) {
            userName = user.name
            avatarUrl = user.avatarUrl.orEmpty()
        }
    }

    /**
     * 로그아웃 처리 후 로그인 화면으로 이동합니다.
     */
    fun logout(navController: NavHostController) {
        viewModelScope.launch {
            try {
                userRepository.logoutAndClearProfile()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("로그아웃에 실패했습니다.")))
                return@launch
            }

            navController.navigate(Screen.SignScreen.route) {
                popUpTo(Screen.SettingScreen.route) { inclusive = true }
            }
        }
    }
}
