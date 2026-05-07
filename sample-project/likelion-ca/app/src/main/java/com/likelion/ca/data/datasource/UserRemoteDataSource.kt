package com.likelion.ca.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.likelion.ca.data.model.UserDto

/**
 * 사용자 프로필 및 상태 관련 원격 데이터 소스 인터페이스입니다.
 */
interface UserRemoteDataSource {
    val currentUser: FirebaseUser?
    fun addAuthStateListener(listener: (FirebaseAuth) -> Unit)
    suspend fun getUserProfile(uid: String): UserDto?
    suspend fun saveUserProfile(user: UserDto)
    suspend fun updateProfile(uid: String, name: String, avatarUrl: String?)
    fun signOut()
}
