package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.result.ApiResult
import com.likelion.ca.domain.error.AppError
import javax.inject.Inject

/**
 * 로그아웃을 수행하고 사용자 프로필 정보를 초기화합니다.
 */
class LogoutUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): ApiResult<Unit> {
        return try {
            userRepository.logoutAndClearProfile()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Failure(AppError.Custom("로그아웃 실패"))
        }
    }
}
