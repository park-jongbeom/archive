package com.likelion.ca.data.datasource

import com.likelion.ca.data.model.ChatMessageDto
import kotlinx.coroutines.flow.Flow

/**
 * 채팅 메시지 수신 및 발신 관련 원격 데이터 소스 인터페이스입니다.
 */
interface ChatMessageRemoteDataSource {
    fun getMessagesFlow(roomId: String): Flow<List<ChatMessageDto>>
    suspend fun sendMessage(message: ChatMessageDto): String
}
