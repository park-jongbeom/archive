package com.likelion.liontalk2.features.chat.ui

import com.likelion.liontalk2.features.chat.data.model.ChatRoom

/**
 * 채팅방 목록 화면에서 표시할 탭 상태입니다.
 */
enum class ChatRoomTab {
    JOINED, NOT_JOINED
}

/**
 * 채팅방 목록 화면의 UI 상태입니다.
 */
data class ChatRoomListState(
    val isLoading : Boolean = false,
    val chatRooms : List<ChatRoom> = emptyList(),
    val joinedRooms : List<ChatRoom> = emptyList(),
    val notJoinedRooms : List<ChatRoom> = emptyList(),
    val currentTab: ChatRoomTab = ChatRoomTab.JOINED,
    val error : String? = null
)
