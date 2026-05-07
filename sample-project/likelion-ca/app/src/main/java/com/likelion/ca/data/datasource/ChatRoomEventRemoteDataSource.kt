package com.likelion.ca.data.datasource

import com.likelion.ca.data.model.RoomEventDto
import kotlinx.coroutines.flow.Flow

/**
 * 채팅방 내 시스템 이벤트(입장, 퇴장 등) 관련 원격 데이터 소스 인터페이스입니다.
 */
interface ChatRoomEventRemoteDataSource {
    fun listenEvents(roomId: String): Flow<List<RoomEventDto>>
    suspend fun sendEvent(roomId: String, event: RoomEventDto)
}
