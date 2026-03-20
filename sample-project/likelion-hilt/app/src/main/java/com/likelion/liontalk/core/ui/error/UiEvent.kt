package com.likelion.liontalk.core.ui.error
/**
 * 화면 스코프에서 처리할 일회성 UI 이벤트입니다.
 */
sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class ShowDialog(
        val title: String = "오류",
        val message: String
    ) : UiEvent
}

