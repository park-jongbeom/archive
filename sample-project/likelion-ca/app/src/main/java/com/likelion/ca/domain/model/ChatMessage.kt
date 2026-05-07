package com.likelion.ca.domain.model

data class ChatMessage(
    val id: String = "",
    val roomId: String = "",
    val sender: ChatUser = ChatUser(),
    val content: String = "",
    val type: String = "text",
    val imageUrl: String? = null,
    val createdAt: Long = 0L,
)
