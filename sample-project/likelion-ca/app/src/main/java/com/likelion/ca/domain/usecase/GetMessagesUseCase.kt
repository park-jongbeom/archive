package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.model.ChatMessage
import com.likelion.ca.domain.repository.ChatMessageRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 특정 채팅방의 메시지 목록을 실시간으로 구독합니다.
 */
class GetMessagesUseCase @Inject constructor(
    private val chatMessageRepository: ChatMessageRepository
) {
    operator fun invoke(roomId: String): Flow<List<ChatMessage>> {
        return chatMessageRepository.getMessagesForRoomFlow(roomId)
    }
}
