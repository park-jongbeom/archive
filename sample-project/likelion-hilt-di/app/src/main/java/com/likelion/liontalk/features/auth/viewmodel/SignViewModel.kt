package com.likelion.liontalk.features.auth.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.likelion.liontalk.core.data.repository.UserRepository
import com.likelion.liontalk.core.navigation.Screen
import com.likelion.liontalk.core.ui.error.UiEvent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.likelion.liontalk.R
import com.likelion.liontalk.features.auth.data.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 로그인 화면에서 네비게이션을 트리거하기 위한 이벤트입니다.
 */
sealed interface SignNavigationEvent {
    /** 지정된 route로 이동하도록 지시합니다. */
    data class NavigateTo(val route: String, val popUpRoute: String? = null) : SignNavigationEvent
}

@HiltViewModel
/**
 * 로그인 화면에서 인증 동작과 로그인 이후 라우팅을 담당하는 ViewModel입니다.
 */
class SignViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private companion object {
        private const val TAG = "SignViewModel"
    }

    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    /** 화면에서 구독하는 일회성 UI 이벤트 스트림입니다. */
    val uiEvents = _uiEvents.asSharedFlow()

    private val _navigationEvents = MutableSharedFlow<SignNavigationEvent>(extraBufferCapacity = 1)
    /** 네비게이션 이동 이벤트 스트림입니다. */
    val navigationEvents = _navigationEvents.asSharedFlow()

    private fun Throwable.toUserMessage(default: String): String {
        val msg = message?.trim()
        return msg?.takeIf { it.isNotBlank() } ?: default
    }

    /** 카카오 로그인 후 로그인 완료 처리를 진행합니다. */
    fun kakaoLogin() {
        viewModelScope.launch {
            try {
                authRepository.kakaoLogin()
                navigateAfterLogin(provider = "kakao")
            } catch (e: Exception) {
                Log.e(TAG, "kakaoLogin failed", e)
                _uiEvents.emit(
                    UiEvent.ShowDialog(
                        title = "로그인 실패",
                        message = e.toUserMessage("로그인에 실패했습니다.")
                    )
                )
            }
        }
    }

    /** 네이버 로그인 후 로그인 완료 처리를 진행합니다. */
    fun naverLogin() {
        viewModelScope.launch {
            try {
                authRepository.naverLogin()
                navigateAfterLogin(provider = "naver")
            } catch (e: Exception) {
                Log.e(TAG, "naverLogin failed", e)
                _uiEvents.emit(
                    UiEvent.ShowDialog(
                        title = "로그인 실패",
                        message = e.toUserMessage("로그인에 실패했습니다.")
                    )
                )
            }
        }
    }

    /**
     * 구글 로그인용 인텐트를 생성하고 launcher로 실행합니다.
     *
     * @param context 구글 로그인 옵션을 생성하는 데 사용됩니다.
     * @param launcher 로그인 결과를 전달받기 위한 ActivityResultLauncher입니다.
     */
    fun googleLogin(
        context: Context,
        launcher: ActivityResultLauncher<Intent>
    ) {
        try {
            val webClientId = context.getString(R.string.default_web_client_id)
            require(webClientId.isNotBlank()) { "default_web_client_id is missing/blank" }

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val client: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
            launcher.launch(client.signInIntent)
        } catch (e: Exception) {
            Log.e(TAG, "googleLogin init failed", e)
        }

    }

    /** GoogleSignIn 결과(Intent)로 Firebase 인증을 수행하고 로그인 후 라우팅을 진행합니다. */
    fun handleGoogleSignInResult(data: Intent) {
        viewModelScope.launch {
            try {
                authRepository.handleGoogleSignInResult(data)
                navigateAfterLogin(provider = "google")

            } catch (e: ApiException) {
                Log.e(TAG, "googleSignIn failed code=${e.statusCode}", e)
                _uiEvents.emit(
                    UiEvent.ShowDialog(
                        title = "로그인 실패",
                        message = e.toUserMessage("로그인에 실패했습니다.")
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "googleSignIn failed", e)
                _uiEvents.emit(
                    UiEvent.ShowDialog(
                        title = "로그인 실패",
                        message = e.toUserMessage("로그인에 실패했습니다.")
                    )
                )
            }
        }
    }

    /** 이메일/비밀번호로 로그인 후 로그인 완료 처리를 진행합니다. */
    fun emailLogin(email: String, password: String) {
        viewModelScope.launch {
            try {
                require(email.isNotBlank()) { "email is blank" }
                require(password.isNotBlank()) { "password is blank" }

                authRepository.signInWithEmail(email.trim(), password)

                navigateAfterLogin(provider = "email")
            } catch (e: Exception) {
                Log.e(TAG, "emailLogin failed", e)
                _uiEvents.emit(
                    UiEvent.ShowDialog(
                        title = "로그인 실패",
                        message = e.toUserMessage("로그인에 실패했습니다.")
                    )
                )
            }
        }
    }

    /** 이메일/비밀번호로 회원가입 후 로그인 완료 처리를 진행합니다. */
    fun emailSignUp(email: String, password: String) {
        viewModelScope.launch {
            try {
                require(email.isNotBlank()) { "email is blank" }
                require(password.isNotBlank()) { "password is blank" }

                authRepository.signUpWithEmail(email.trim(), password)

                navigateAfterLogin(provider = "email")
            } catch (e: Exception) {
                Log.e(TAG, "emailSignUp failed", e)
                _uiEvents.emit(
                    UiEvent.ShowDialog(
                        title = "로그인 실패",
                        message = e.toUserMessage("회원가입/로그인에 실패했습니다.")
                    )
                )
            }
        }
    }

    private suspend fun navigateAfterLogin(provider: String) {
        // 로그인 직후 Firestore 프로필이 준비되지 않았을 수 있어 ensure를 먼저 호출한다.
        withContext(Dispatchers.IO) {
            userRepository.ensureUserProfile(provider = provider)
        }

        val user = userRepository.meOrNull
        val destination = if (user == null || user.name.isBlank()) {
            Screen.SettingScreen.route
        } else {
            Screen.ChatRoomListScreen.route
        }

        _navigationEvents.emit(
            SignNavigationEvent.NavigateTo(
                route = destination,
                popUpRoute = Screen.SignScreen.route
            )
        )
    }
}
