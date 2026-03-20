package com.likelion.liontalk.features.chat.ui

import com.likelion.liontalk.core.model.ChatUser

/**
 * 채팅방 화면에서 구독하는 UI 전환 이벤트(타이핑/입장/퇴장/스크롤 등)입니다.
 */
sealed class ChatRoomEvent {
    data class TypingStarted(val sender: ChatUser) : ChatRoomEvent()
    object TypingStopped : ChatRoomEvent()
    data class ChatRoomEnter(val sender: ChatUser) : ChatRoomEvent()
    data class ChatRoomLeave(val sender: ChatUser) : ChatRoomEvent()
    object ScrollToBottom : ChatRoomEvent()
    object ClearInput : ChatRoomEvent()
    object Kicked : ChatRoomEvent()

    object Exploded : ChatRoomEvent()

    object MessageReceived

}
