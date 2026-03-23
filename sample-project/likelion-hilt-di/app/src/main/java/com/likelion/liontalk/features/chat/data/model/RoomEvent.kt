package com.likelion.liontalk.features.chat.data.model

import com.likelion.liontalk.core.data.model.ChatUser

/**
 * 채팅방에서 발생하는 이벤트 모델입니다.
 *
 * Firestore 이벤트 문서의 값을 앱에서 처리하기 위한 형태로 표현합니다.
 */
data class RoomEvent(
    val id: String = "",
    val type: String = "",
    // Legacy fields (기존 데이터 호환)
    val sender: String? = null,
    val target: String? = null,
    // New fields (id/name/avatarUrl 포함)
    val senderUser: ChatUser? = null,
    val targetUser: ChatUser? = null,
    val typing: Boolean? = null,
    val timestamp: Long = 0L
)

