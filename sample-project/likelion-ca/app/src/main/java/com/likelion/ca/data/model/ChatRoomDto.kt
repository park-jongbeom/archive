package com.likelion.ca.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Firestore와 통신할 때 사용하는 채팅방 데이터 객체입니다.
 */
data class ChatRoomDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("title") @set:PropertyName("title") var title: String = "",
    @get:PropertyName("owner") @set:PropertyName("owner") var owner: UserDto = UserDto(),
    @get:PropertyName("users") @set:PropertyName("users") var users: List<UserDto> = emptyList(),
    @get:PropertyName("unReadCount") @set:PropertyName("unReadCount") var unReadCount: Int = 0,
    @get:PropertyName("lastReadMessageId") @set:PropertyName("lastReadMessageId") var lastReadMessageId: String = "",
    @get:PropertyName("lastReadMessageTimestamp") @set:PropertyName("lastReadMessageTimestamp") var lastReadMessageTimestamp: Long = 0L,
    @get:PropertyName("isLocked") @set:PropertyName("isLocked") var isLocked: Boolean = false,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = 0L,
)
