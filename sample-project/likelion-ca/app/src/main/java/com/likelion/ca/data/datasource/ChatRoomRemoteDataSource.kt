package com.likelion.ca.data.datasource

import com.likelion.ca.data.model.ChatRoomDto

/**
 * 채팅방 목록 및 메타데이터 관련 원격 데이터 소스 인터페이스입니다.
 */
interface ChatRoomRemoteDataSource {
    suspend fun getRooms(): List<ChatRoomDto>
    suspend fun getRoom(roomId: String): ChatRoomDto?
    suspend fun createRoom(room: ChatRoomDto)
    suspend fun updateRoom(room: ChatRoomDto)
}
