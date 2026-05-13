package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.model.ChatUser
import com.likelion.ca.domain.repository.ChatRoomEventRepository
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.result.ApiResult
import com.likelion.ca.domain.error.AppError
import javax.inject.Inject

/**
 * 채팅방에 시스템 이벤트를 발행합니다.
 */
class SendRoomEventUseCase @Inject constructor(
    private val eventRepository: ChatRoomEventRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        roomId: String,
        type: String,
        target: ChatUser? = null,
        typing: Boolean? = null
    ): ApiResult<Unit> {
        return try {
            val me = userRepository.meOrNull ?: return ApiResult.Failure(AppError.Auth())
            eventRepository.sendEvent(roomId, type, me, target, typing)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Failure(AppError.Custom("이벤트 전송 실패: $type"))
        }
    }
}
