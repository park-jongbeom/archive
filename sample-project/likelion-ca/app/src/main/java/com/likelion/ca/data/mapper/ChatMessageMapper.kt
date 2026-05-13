package com.likelion.ca.data.mapper

import com.likelion.ca.data.model.ChatMessageDto
import com.likelion.ca.domain.model.ChatMessage

/**
 * ChatMessageDto와 ChatMessage 간의 변환을 담당하는 매퍼입니다.
 */
fun ChatMessageDto.toDomain(): ChatMessage = ChatMessage(
    id = id,
    roomId = roomId,
    sender = sender.toDomain(),
    message = message,
    imageUrl = imageUrl,
    createdAt = createdAt
)

fun ChatMessage.toDto(): ChatMessageDto = ChatMessageDto(
    id = id,
    roomId = roomId,
    sender = sender.toDto(),
    message = message,
    imageUrl = imageUrl,
    createdAt = createdAt
)
