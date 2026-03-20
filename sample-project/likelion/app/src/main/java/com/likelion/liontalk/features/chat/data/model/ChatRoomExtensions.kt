package com.likelion.liontalk2.features.chat.data.model

import com.likelion.liontalk2.core.model.ChatUser

/**
 * id가 있으면 id로 우선 비교하고, 기존 데이터/마이그레이션 때문에 id가 비어있으면 name으로 fallback한다.
 */
fun ChatUser.isSameUser(other: ChatUser?): Boolean {
    val thisId = id
    val otherId = other?.id
    if (thisId.isNotBlank() && !otherId.isNullOrBlank()) {
        return thisId == otherId
    }
    return name == other?.name
}

/**
 * 사용자가 채팅방에 입장할 때, 기존 사용자 목록에 중복 없이 추가합니다.
 */
fun ChatRoom.addUserIfNotExists(user: ChatUser): ChatRoom {
    val updatedUsers = this.users.toMutableList().apply {
        if (none { it.isSameUser(user) }) add(user)
    }
    return this.copy(users = updatedUsers)
}

/**
 * 사용자가 채팅방에서 퇴장할 때, 기존 사용자 목록에서 해당 사용자를 제거합니다.
 */
fun ChatRoom.removeUser(user: ChatUser): ChatRoom {
    val updatedUsers = this.users.filterNot { it.isSameUser(user) }
    return this.copy(users = updatedUsers)
}

