package com.likelion.ca.presentation.launcher.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.likelion.ca.core.navigation.toRoute
import com.likelion.ca.domain.usecase.ComputeStartDestinationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
/**
 * 앱 시작 시 인증 상태를 확인하고 최초 라우팅 목적지를 결정하는 ViewModel입니다.
 */
class LauncherViewModel @Inject constructor(
    private val computeStartDestinationUseCase: ComputeStartDestinationUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LauncherUiState>(LauncherUiState.Initial)
    val uiState: StateFlow<LauncherUiState> = _uiState.asStateFlow()

    fun decideStartDestination() {
        viewModelScope.launch {
            _uiState.value = LauncherUiState.Loading
            try {
                val destination = computeStartDestinationUseCase().toRoute()
                _uiState.value = LauncherUiState.Success(destination)
            } catch (e: Exception) {
                val msg = e.message?.trim()?.takeIf { it.isNotBlank() } ?: "로그인 정보를 불러오지 못했어요."
                _uiState.value = LauncherUiState.Error(msg)
            }
        }
    }
}
