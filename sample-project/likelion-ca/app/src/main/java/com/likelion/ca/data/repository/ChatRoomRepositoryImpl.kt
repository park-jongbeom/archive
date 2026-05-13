package com.likelion.ca.data.repository

import com.likelion.ca.data.datasource.ChatRoomRemoteDataSource
import com.likelion.ca.data.mapper.toDomain
import com.likelion.ca.data.mapper.toDto
import com.likelion.ca.domain.model.ChatRoom
import com.likelion.ca.domain.model.ChatUser
import com.likelion.ca.domain.model.addUserIfNotExists
import com.likelion.ca.domain.model.removeUser
import com.likelion.ca.domain.repository.ChatRoomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRoomRepositoryImpl @Inject constructor(
    private val chatRoomRemoteDataSource: ChatRoomRemoteDataSource
) : ChatRoomRepository {

    override suspend fun fetchRoomsOnce(): List<ChatRoom> {
        return chatRoomRemoteDataSource.getRooms().map { it.toDomain() }
    }

    override suspend fun createChatRoom(chatRoom: ChatRoom) {
        chatRoomRemoteDataSource.createRoom(chatRoom.toDto())
    }

    override suspend fun deleteChatRoomToRemote(roomId: String) {
        chatRoomRemoteDataSource.deleteRoom(roomId)
    }

    override suspend fun enterRoom(user: ChatUser, roomId: String): ChatRoom {
        val remoteDto = chatRoomRemoteDataSource.getRoom(roomId)
            ?: throw Exception("Room not found: $roomId")
        
        val room = remoteDto.toDomain().addUserIfNotExists(user)
        chatRoomRemoteDataSource.updateRoom(room.toDto())
        return room
    }

    override fun getChatRoomFlow(roomId: String): Flow<ChatRoom> = flow {
        val room = chatRoomRemoteDataSource.getRoom(roomId)?.toDomain()
            ?: throw Exception("Room not found: $roomId")
        emit(room)
    }

    override suspend fun getRoomFromRemote(roomId: String): ChatRoom? {
        return chatRoomRemoteDataSource.getRoom(roomId)?.toDomain()
    }

    override suspend fun removeUserFromRoom(user: ChatUser, roomId: String) {
        val remoteDto = chatRoomRemoteDataSource.getRoom(roomId)
            ?: throw Exception("Room not found: $roomId")
        
        val room = remoteDto.toDomain().removeUser(user)
        chatRoomRemoteDataSource.updateRoom(room.toDto())
    }

    override suspend fun toggleLock(isLock: Boolean, roomId: String) {
        val remoteDto = chatRoomRemoteDataSource.getRoom(roomId) ?: return
        val room = remoteDto.toDomain().copy(isLocked = isLock)
        chatRoomRemoteDataSource.updateRoom(room.toDto())
    }
}
