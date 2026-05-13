package com.likelion.ca.data.repository

import com.likelion.ca.data.datasource.ChatMessageRemoteDataSource
import com.likelion.ca.data.mapper.toDomain
import com.likelion.ca.data.mapper.toDto
import com.likelion.ca.domain.model.ChatMessage
import com.likelion.ca.domain.repository.ChatMessageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatMessageRepositoryImpl @Inject constructor(
    private val chatMessageRemoteDataSource: ChatMessageRemoteDataSource
) : ChatMessageRepository {

    override fun getMessagesForRoomFlow(roomId: String): Flow<List<ChatMessage>> {
        return chatMessageRemoteDataSource.getMessagesFlow(roomId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun sendMessage(message: ChatMessage): ChatMessage {
        val id = chatMessageRemoteDataSource.sendMessage(message.toDto())
        return message.copy(id = id)
    }
}
