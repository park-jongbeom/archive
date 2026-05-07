package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.model.ChatMessage
import com.likelion.ca.domain.repository.ChatMessageRepository
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.result.ApiResult
import com.likelion.ca.domain.error.AppError
import javax.inject.Inject

/**
 * 채팅 메시지를 전송하는 비즈니스 로직을 수행합니다.
 */
class SendMessageUseCase @Inject constructor(
    private val chatMessageRepository: ChatMessageRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(roomId: String, text: String, imageUrl: String? = null): ApiResult<ChatMessage> {
        return try {
            val me = userRepository.meOrNull ?: return ApiResult.Failure(AppError.Auth())
            
            val message = ChatMessage(
                roomId = roomId,
                sender = me,
                message = text,
                imageUrl = imageUrl,
                createdAt = System.currentTimeMillis()
            )
            
            val sentMessage = chatMessageRepository.sendMessage(message)
            ApiResult.Success(sentMessage)
        } catch (e: Exception) {
            ApiResult.Failure(AppError.Custom("메시지 전송 실패"))
        }
    }
}
