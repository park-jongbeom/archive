package com.likelion.ca.data.mapper

import com.likelion.ca.data.model.UserDto
import com.likelion.ca.domain.model.ChatUser

/**
 * UserDto와 ChatUser 간의 변환을 담당하는 매퍼입니다.
 */
fun UserDto.toDomain(): ChatUser = ChatUser(
    id = id,
    name = name,
    avatarUrl = avatarUrl.takeIf { it.isNotBlank() }
)

fun ChatUser.toDto(provider: String = ""): UserDto = UserDto(
    id = id,
    name = name,
    avatarUrl = avatarUrl ?: "",
    provider = provider
)
