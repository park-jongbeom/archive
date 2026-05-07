package com.likelion.ca.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Firestore와 통신할 때 사용하는 채팅 메시지 데이터 객체입니다.
 */
data class ChatMessageDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("roomId") @set:PropertyName("roomId") var roomId: String = "",
    @get:PropertyName("sender") @set:PropertyName("sender") var sender: UserDto = UserDto(),
    @get:PropertyName("message") @set:PropertyName("message") var message: String = "",
    @get:PropertyName("imageUrl") @set:PropertyName("imageUrl") var imageUrl: String? = null,
    @get:PropertyName("createdAt") @set:PropertyName("createdAt") var createdAt: Long = 0L,
)
