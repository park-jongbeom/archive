package com.likelion.ca.domain.repository

import com.likelion.ca.domain.model.AuthSession
import com.likelion.ca.domain.model.ChatUser
import kotlinx.coroutines.flow.StateFlow

/**
 * 인증 세션·프로필 동기화 계약 (구현은 data 모듈).
 */
interface UserRepository {
    val authSession: StateFlow<AuthSession?>
    val me: StateFlow<ChatUser?>
    val meOrNull: ChatUser?

    fun start()
    suspend fun ensureUserProfile(provider: String? = null)
    suspend fun logoutAndClearProfile()
    fun requireMe(): ChatUser
    suspend fun updateProfile(uid: String, name: String, avatarUrl: String?)
}
