package com.likelion.ca.data.mapper

import com.likelion.ca.data.model.RoomEventDto
import com.likelion.ca.domain.model.RoomEvent

/**
 * RoomEventDto와 RoomEvent 간의 변환을 담당하는 매퍼입니다.
 */
fun RoomEventDto.toDomain(): RoomEvent = RoomEvent(
    id = id,
    type = type,
    sender = sender,
    senderUser = senderUser?.toDomain(),
    target = target,
    targetUser = targetUser?.toDomain(),
    typing = typing,
    timestamp = timestamp
)

fun RoomEvent.toDto(): RoomEventDto = RoomEventDto(
    id = id,
    type = type,
    sender = sender ?: "",
    senderUser = senderUser?.toDto(),
    target = target,
    targetUser = targetUser?.toDto(),
    typing = typing,
    timestamp = timestamp
)
