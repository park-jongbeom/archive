package com.likelion.ca.presentation.chat.ui

import com.likelion.ca.domain.model.ChatMessage
import com.likelion.ca.domain.model.ChatRoom

/**
 * 채팅방 상세 화면의 UI 상태입니다.
 */
data class ChatRoomUiState(
    val isLoading: Boolean = false,
    val room: ChatRoom? = null,
    val messages: List<ChatMessage> = emptyList(),
    val explodeState: Boolean = false,
    val error: String? = null
)
