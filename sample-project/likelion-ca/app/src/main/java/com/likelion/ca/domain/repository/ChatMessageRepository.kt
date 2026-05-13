package com.likelion.ca.domain.repository

import com.likelion.ca.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatMessageRepository {
    fun getMessagesForRoomFlow(roomId: String): Flow<List<ChatMessage>>
    suspend fun sendMessage(message: ChatMessage): ChatMessage
}
