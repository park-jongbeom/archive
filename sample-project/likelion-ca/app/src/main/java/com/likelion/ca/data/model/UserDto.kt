package com.likelion.ca.data.model

import com.google.firebase.firestore.PropertyName

/**
 * Firestore와 통신할 때 사용하는 사용자 데이터 객체입니다.
 */
data class UserDto(
    @get:PropertyName("id") @set:PropertyName("id") var id: String = "",
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("avatarUrl") @set:PropertyName("avatarUrl") var avatarUrl: String = "",
    @get:PropertyName("provider") @set:PropertyName("provider") var provider: String = "",
)
