package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.repository.AuthRepository
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.result.ApiResult
import com.likelion.ca.domain.error.AppError
import javax.inject.Inject

/**
 * 소셜 로그인을 수행하고 프로필을 동기화하는 UseCase입니다.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) {
    suspend fun google(idToken: String): ApiResult<Unit> = runCatching {
        authRepository.signInWithGoogleIdToken(idToken)
        userRepository.ensureUserProfile(provider = "google")
    }

    suspend fun kakao(): ApiResult<Unit> = runCatching {
        authRepository.kakaoLogin()
        userRepository.ensureUserProfile(provider = "kakao")
    }

    private suspend fun <T> runCatching(action: suspend () -> T): ApiResult<T> {
        return try {
            ApiResult.Success(action())
        } catch (e: Exception) {
            ApiResult.Failure(if (e is AppError) e else AppError.Custom(e.message ?: "로그인 실패"))
        }
    }
}
