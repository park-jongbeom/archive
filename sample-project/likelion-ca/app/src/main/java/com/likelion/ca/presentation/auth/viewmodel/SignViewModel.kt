package com.likelion.ca.presentation.auth.viewmodel

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.likelion.ca.R
import com.likelion.ca.core.navigation.Screen
import com.likelion.ca.core.navigation.toRoute
import com.likelion.ca.core.ui.error.UiEvent
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.usecase.AppStartDestination
import com.likelion.ca.domain.usecase.ComputeStartDestinationUseCase
import com.likelion.ca.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SignNavigationEvent {
    data class NavigateTo(val route: String, val popUpRoute: String? = null) : SignNavigationEvent
}

@HiltViewModel
class SignViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val userRepository: UserRepository,
    private val computeStartDestinationUseCase: ComputeStartDestinationUseCase,
) : ViewModel() {

    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvents = _uiEvents.asSharedFlow()

    private val _navigationEvents = MutableSharedFlow<SignNavigationEvent>(extraBufferCapacity = 1)
    val navigationEvents = _navigationEvents.asSharedFlow()

    fun kakaoLogin() {
        viewModelScope.launch {
            loginUseCase.kakao().onSuccess {
                navigateAfterLogin()
            }.onFailure { error ->
                _uiEvents.emit(UiEvent.ShowDialog(title = "로그인 실패", message = error.message))
            }
        }
    }

    fun googleLogin(context: Context, launcher: ActivityResultLauncher<Intent>) {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            val client: GoogleSignInClient = GoogleSignIn.getClient(context, gso)
            launcher.launch(client.signInIntent)
        } catch (e: Exception) {
            Log.e("SignViewModel", "googleLogin init failed", e)
        }
    }

    fun handleGoogleSignInResult(data: Intent) {
        viewModelScope.launch {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(data).getResult(ApiException::class.java)
                val idToken = account.idToken ?: throw Exception("idToken null")
                
                loginUseCase.google(idToken).onSuccess {
                    navigateAfterLogin()
                }.onFailure { error ->
                    _uiEvents.emit(UiEvent.ShowDialog(title = "로그인 실패", message = error.message))
                }
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowDialog(title = "로그인 실패", message = "구글 로그인에 실패했습니다."))
            }
        }
    }

    private suspend fun navigateAfterLogin() {
        val destination = computeStartDestinationUseCase().toRoute()
        _navigationEvents.emit(
            SignNavigationEvent.NavigateTo(
                route = destination,
                popUpRoute = Screen.SignScreen.route
            )
        )
    }

    suspend fun getRedirectDestinationIfSignedIn(): String? {
        if (userRepository.authSession.value == null) return null
        val destination = computeStartDestinationUseCase()
        return if (destination == AppStartDestination.Sign) null else destination.toRoute()
    }
}
