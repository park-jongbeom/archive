package com.likelion.ca.domain.model

data class ChatRoom(
    val id: String = "",
    val title: String = "",
    val owner: ChatUser = ChatUser(),
    val users: List<ChatUser> = emptyList(),
    val unReadCount: Int = 0,
    val lastReadMessageId: String = "",
    val lastReadMessageTimestamp: Long = 0L,
    val isLocked: Boolean = false,
    val createdAt: Long = 0L,
)
