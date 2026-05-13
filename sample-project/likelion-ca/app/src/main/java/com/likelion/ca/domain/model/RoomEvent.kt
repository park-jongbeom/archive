package com.likelion.ca.domain.model

data class RoomEvent(
    val id: String = "",
    val type: String = "",
    val sender: String? = null,
    val target: String? = null,
    val senderUser: ChatUser? = null,
    val targetUser: ChatUser? = null,
    val typing: Boolean? = null,
    val timestamp: Long = 0L,
)
