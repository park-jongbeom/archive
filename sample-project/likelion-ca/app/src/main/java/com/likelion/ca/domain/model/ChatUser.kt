package com.likelion.ca.domain.model

/**
 * 채팅 도메인에서 사용하는 사용자 엔티티입니다. (UI/Firestore와 분리)
 */
data class ChatUser(
    val id: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
)
