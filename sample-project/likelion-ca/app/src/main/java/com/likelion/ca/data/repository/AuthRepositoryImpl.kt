package com.likelion.ca.data.repository

import com.likelion.ca.data.datasource.AuthRemoteDataSource
import com.likelion.ca.domain.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authRemoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override suspend fun kakaoLogin() {
        val accessToken = authRemoteDataSource.loginWithKakao()
        authRemoteDataSource.signInWithCustomToken("kakaoCustomAuth", accessToken)
    }

    override suspend fun naverLogin() {
        val accessToken = authRemoteDataSource.loginWithNaver()
        authRemoteDataSource.signInWithCustomToken("naverCustomAuth", accessToken)
    }

    override suspend fun signInWithGoogleIdToken(idToken: String) {
        authRemoteDataSource.signInWithGoogle(idToken)
    }

    override suspend fun signUpWithEmail(email: String, password: String) {
        authRemoteDataSource.signUpWithEmail(email, password)
    }

    override suspend fun signInWithEmail(email: String, password: String) {
        authRemoteDataSource.signInWithEmail(email, password)
    }
}
