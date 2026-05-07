package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.model.ChatRoom
import com.likelion.ca.domain.model.isSameUser
import com.likelion.ca.domain.repository.ChatRoomRepository
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.result.ApiResult
import com.likelion.ca.domain.error.AppError
import javax.inject.Inject

/**
 * 채팅방 목록을 가져오고 참여 여부에 따라 분류하는 비즈니스 로직을 수행합니다.
 */
class GetChatRoomsUseCase @Inject constructor(
    private val chatRoomRepository: ChatRoomRepository,
    private val userRepository: UserRepository
) {
    data class ChatRoomsResult(
        val all: List<ChatRoom>,
        val joined: List<ChatRoom>,
        val notJoined: List<ChatRoom>
    )

    suspend operator fun invoke(): ApiResult<ChatRoomsResult> {
        return try {
            val me = userRepository.meOrNull ?: return ApiResult.Failure(AppError.Auth())
            val rooms = chatRoomRepository.fetchRoomsOnce()
            
            val joined = rooms.filter { room -> room.users.any { user -> user.isSameUser(me) } }
            val notJoined = rooms.filter { room -> room.users.none { user -> user.isSameUser(me) } }
            
            ApiResult.Success(
                ChatRoomsResult(all = rooms, joined = joined, notJoined = notJoined)
            )
        } catch (e: Exception) {
            ApiResult.Failure(AppError.Custom(e.message ?: "채팅방 로드 실패"))
        }
    }
}
