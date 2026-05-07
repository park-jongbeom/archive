package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.repository.ChatRoomEventRepository
import com.likelion.ca.domain.repository.ChatRoomRepository
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.result.ApiResult
import com.likelion.ca.domain.error.AppError
import javax.inject.Inject

/**
 * 채팅방에서 퇴장하는 비즈니스 로직을 수행합니다.
 */
class LeaveRoomUseCase @Inject constructor(
    private val chatRoomRepository: ChatRoomRepository,
    private val eventRepository: ChatRoomEventRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(roomId: String): ApiResult<Unit> {
        return try {
            val me = userRepository.meOrNull ?: return ApiResult.Failure(AppError.Auth())
            
            // 1. 원격 이벤트 발행
            eventRepository.sendEvent(roomId, "leave", me)
            
            // 2. 방에서 사용자 제거
            chatRoomRepository.removeUserFromRoom(me, roomId)
            
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Failure(AppError.Custom("채팅방 퇴장 실패"))
        }
    }
}
