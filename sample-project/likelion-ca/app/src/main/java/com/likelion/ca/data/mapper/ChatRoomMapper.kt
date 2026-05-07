package com.likelion.ca.data.mapper

import com.likelion.ca.data.model.ChatRoomDto
import com.likelion.ca.domain.model.ChatRoom

/**
 * ChatRoomDto와 ChatRoom 간의 변환을 담당하는 매퍼입니다.
 */
fun ChatRoomDto.toDomain(): ChatRoom = ChatRoom(
    id = id,
    title = title,
    owner = owner.toDomain(),
    users = users.map { it.toDomain() },
    unReadCount = unReadCount,
    lastReadMessageId = lastReadMessageId,
    lastReadMessageTimestamp = lastReadMessageTimestamp,
    isLocked = isLocked,
    createdAt = createdAt
)

fun ChatRoom.toDto(): ChatRoomDto = ChatRoomDto(
    id = id,
    title = title,
    owner = owner.toDto(),
    users = users.map { it.toDto() },
    unReadCount = unReadCount,
    lastReadMessageId = lastReadMessageId,
    lastReadMessageTimestamp = lastReadMessageTimestamp,
    isLocked = isLocked,
    createdAt = createdAt
)
