package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.model.ChatRoom
import com.likelion.ca.domain.repository.ChatRoomRepository
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.result.ApiResult
import com.likelion.ca.domain.error.AppError
import javax.inject.Inject

/**
 * 채팅방에 입장하는 비즈니스 로직을 수행합니다. (잠금 확인, 사용자 추가 등)
 */
class EnterChatRoomUseCase @Inject constructor(
    private val chatRoomRepository: ChatRoomRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(roomId: String): ApiResult<ChatRoom> {
        return try {
            val me = userRepository.meOrNull ?: return ApiResult.Failure(AppError.Auth())
            val room = chatRoomRepository.getRoomFromRemote(roomId) 
                ?: return ApiResult.Failure(AppError.Custom("채팅방을 찾을 수 없습니다."))
            
            if (room.isLocked && room.owner.id != me.id) {
                return ApiResult.Failure(AppError.Custom("잠긴 채팅방입니다."))
            }

            val enteredRoom = chatRoomRepository.enterRoom(me, roomId)
            ApiResult.Success(enteredRoom)
        } catch (e: Exception) {
            ApiResult.Failure(AppError.Custom(e.message ?: "입장 실패"))
        }
    }
}
