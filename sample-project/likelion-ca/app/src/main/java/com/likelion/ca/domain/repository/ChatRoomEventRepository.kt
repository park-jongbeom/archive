package com.likelion.ca.domain.repository

import com.likelion.ca.domain.model.ChatUser
import com.likelion.ca.domain.model.RoomEvent
import kotlinx.coroutines.flow.Flow

interface ChatRoomEventRepository {
    fun listenEvents(roomId: String): Flow<List<RoomEvent>>
    suspend fun sendEvent(
        roomId: String,
        type: String,
        sender: ChatUser,
        target: ChatUser? = null,
        typing: Boolean? = null,
    )
}
