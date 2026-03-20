package com.likelion.liontalk2.features.chat.data.model

import com.likelion.liontalk2.core.model.ChatUser

/**
 * 메시지 모델(Firestore <-> UI).
 */
data class ChatMessage(
    val id: String = "",
    val roomId: String = "",
    val sender: ChatUser = ChatUser("", ""),
    val content: String = "",
    val type: String = "text",
    val imageUrl: String? = null,
    val createdAt: Long = 0L
)

