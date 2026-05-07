package com.likelion.ca.domain.model

/**
 * id가 있으면 id로 우선 비교하고, 기존 데이터 호환을 위해 id가 비어 있으면 name으로 비교합니다.
 */
fun ChatUser.isSameUser(other: ChatUser?): Boolean {
    val thisId = id
    val otherId = other?.id
    if (thisId.isNotBlank() && !otherId.isNullOrBlank()) {
        return thisId == otherId
    }
    return name == other?.name
}

fun ChatRoom.addUserIfNotExists(user: ChatUser): ChatRoom {
    val updatedUsers = users.toMutableList().apply {
        if (none { it.isSameUser(user) }) add(user)
    }
    return copy(users = updatedUsers)
}

fun ChatRoom.removeUser(user: ChatUser): ChatRoom {
    val updatedUsers = users.filterNot { it.isSameUser(user) }
    return copy(users = updatedUsers)
}
