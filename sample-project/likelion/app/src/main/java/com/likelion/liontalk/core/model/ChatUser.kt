package com.likelion.liontalk2.core.model

/**
 * 채팅 화면에서 사용하는 사용자 모델입니다.
 */
data class ChatUser(
    val name: String = "",
    val avatarUrl: String? = "",
    val id: String = ""
)
