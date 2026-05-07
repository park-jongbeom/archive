package com.likelion.ca.domain.repository

/**
 * 소셜/이메일 로그인 계약. 구글은 idToken만 domain 경계로 넘깁니다.
 */
interface AuthRepository {
    suspend fun kakaoLogin()
    suspend fun naverLogin()
    suspend fun signInWithGoogleIdToken(idToken: String)
    suspend fun signUpWithEmail(email: String, password: String)
    suspend fun signInWithEmail(email: String, password: String)
}
