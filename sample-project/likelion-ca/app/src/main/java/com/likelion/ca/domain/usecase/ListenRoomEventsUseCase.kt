package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.model.RoomEvent
import com.likelion.ca.domain.repository.ChatRoomEventRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 특정 채팅방에서 발생하는 시스템 이벤트(입장, 퇴장, 타이핑 등)를 실시간으로 구독합니다.
 */
class ListenRoomEventsUseCase @Inject constructor(
    private val eventRepository: ChatRoomEventRepository
) {
    operator fun invoke(roomId: String): Flow<List<RoomEvent>> {
        return eventRepository.listenEvents(roomId)
    }
}
