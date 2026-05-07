package com.likelion.ca.domain.repository

import com.likelion.ca.domain.model.ChatRoom
import com.likelion.ca.domain.model.ChatUser
import kotlinx.coroutines.flow.Flow

interface ChatRoomRepository {
    suspend fun fetchRoomsOnce(): List<ChatRoom>
    suspend fun createChatRoom(chatRoom: ChatRoom)
    suspend fun deleteChatRoomToRemote(roomId: String)
    suspend fun enterRoom(user: ChatUser, roomId: String): ChatRoom
    fun getChatRoomFlow(roomId: String): Flow<ChatRoom>
    suspend fun getRoomFromRemote(roomId: String): ChatRoom?
    suspend fun removeUserFromRoom(user: ChatUser, roomId: String)
    suspend fun toggleLock(isLock: Boolean, roomId: String)
}
