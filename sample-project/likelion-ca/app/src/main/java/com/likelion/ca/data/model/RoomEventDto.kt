package com.likelion.ca.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Firestore와 통신할 때 사용하는 채팅방 이벤트 데이터 객체입니다.
 */
data class RoomEventDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("type") @set:PropertyName("type") var type: String = "",
    @get:PropertyName("sender") @set:PropertyName("sender") var sender: String = "",
    @get:PropertyName("senderUser") @set:PropertyName("senderUser") var senderUser: UserDto? = null,
    @get:PropertyName("target") @set:PropertyName("target") var target: String? = null,
    @get:PropertyName("targetUser") @set:PropertyName("targetUser") var targetUser: UserDto? = null,
    @get:PropertyName("typing") @set:PropertyName("typing") var typing: Boolean? = null,
    @get:PropertyName("timestamp") @set:PropertyName("timestamp") var timestamp: Long = 0L,
)
