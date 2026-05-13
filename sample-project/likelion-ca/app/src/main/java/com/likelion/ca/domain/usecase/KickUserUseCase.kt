package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.model.ChatUser
import com.likelion.ca.domain.repository.ChatRoomEventRepository
import com.likelion.ca.domain.repository.ChatRoomRepository
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.result.ApiResult
import com.likelion.ca.domain.error.AppError
import javax.inject.Inject

/**
 * 특정 사용자를 채팅방에서 추방하는 비즈니스 로직을 수행합니다.
 */
class KickUserUseCase @Inject constructor(
    private val chatRoomRepository: ChatRoomRepository,
    private val eventRepository: ChatRoomEventRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(roomId: String, target: ChatUser): ApiResult<Unit> {
        return try {
            val me = userRepository.meOrNull ?: return ApiResult.Failure(AppError.Auth())
            
            // 1. 권한 체크 (방장만 가능)
            val room = chatRoomRepository.getRoomFromRemote(roomId)
                ?: return ApiResult.Failure(AppError.Custom("채팅방을 찾을 수 없습니다."))
            
            if (room.owner.id != me.id) {
                return ApiResult.Failure(AppError.Custom("추방 권한이 없습니다."))
            }

            // 2. 사용자 제거
            chatRoomRepository.removeUserFromRoom(target, roomId)
            
            // 3. 원격 이벤트 발행
            eventRepository.sendEvent(roomId, "kick", me, target)
            
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Failure(AppError.Custom("추방 실패"))
        }
    }
}
