package com.likelion.ca.domain.model

/**
 * Firebase Auth 등 외부 인증 구현과 무관한 세션 스냅샷입니다.
 */
data class AuthSession(
    val uid: String,
    val email: String?,
    val displayName: String?,
)
