package com.likelion.liontalk.features.chat.data.model

import com.likelion.liontalk.core.model.ChatUser

/**
 * 채팅방 모델(Firestore <-> UI).
 * - Entity/DAO는 사용하지 않고 data class만 유지
 */
data class ChatRoom(
    val id: String = "",
    val title: String = "",
    val owner: ChatUser = ChatUser("", ""),
    val users: List<ChatUser> = emptyList(),
    val unReadCount: Int = 0,
    val lastReadMessageId: String = "",
    val lastReadMessageTimestamp: Long = 0L,
    val isLocked: Boolean = false,
    val createdAt: Long = 0L
)

