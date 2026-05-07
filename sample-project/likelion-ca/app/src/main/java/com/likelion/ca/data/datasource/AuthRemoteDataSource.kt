package com.likelion.ca.data.datasource

/**
 * 소셜 및 이메일 인증 관련 원격 데이터 소스 인터페이스입니다.
 */
interface AuthRemoteDataSource {
    suspend fun loginWithKakao(): String
    suspend fun loginWithNaver(): String
    suspend fun signInWithGoogle(idToken: String)
    suspend fun signInWithCustomToken(functionName: String, accessToken: String)
    suspend fun signUpWithEmail(email: String, password: String)
    suspend fun signInWithEmail(email: String, password: String)
}
