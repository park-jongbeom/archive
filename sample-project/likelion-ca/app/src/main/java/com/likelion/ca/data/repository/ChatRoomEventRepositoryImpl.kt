package com.likelion.ca.data.repository

import com.likelion.ca.data.datasource.ChatRoomEventRemoteDataSource
import com.likelion.ca.data.mapper.toDomain
import com.likelion.ca.data.mapper.toDto
import com.likelion.ca.domain.model.ChatUser
import com.likelion.ca.domain.model.RoomEvent
import com.likelion.ca.domain.repository.ChatRoomEventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRoomEventRepositoryImpl @Inject constructor(
    private val chatRoomEventRemoteDataSource: ChatRoomEventRemoteDataSource
) : ChatRoomEventRepository {

    override fun listenEvents(roomId: String): Flow<List<RoomEvent>> {
        return chatRoomEventRemoteDataSource.listenEvents(roomId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun sendEvent(
        roomId: String,
        type: String,
        sender: ChatUser,
        target: ChatUser?,
        typing: Boolean?
    ) {
        val event = RoomEvent(
            type = type,
            sender = sender.name,
            senderUser = sender,
            target = target?.name,
            targetUser = target,
            typing = typing,
            timestamp = System.currentTimeMillis()
        )
        chatRoomEventRemoteDataSource.sendEvent(roomId, event.toDto())
    }
}
