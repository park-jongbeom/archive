package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.repository.StorageRepository
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.result.ApiResult
import com.likelion.ca.domain.error.AppError
import javax.inject.Inject

/**
 * 채팅방 이미지를 업로드하고 URL을 반환합니다.
 */
class UploadChatImageUseCase @Inject constructor(
    private val storageRepository: StorageRepository,
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(roomId: String, localUri: String): ApiResult<String> {
        return try {
            val me = userRepository.meOrNull ?: return ApiResult.Failure(AppError.Auth())
            val imageUrl = storageRepository.uploadChatImage(roomId, me.name, localUri)
            ApiResult.Success(imageUrl)
        } catch (e: Exception) {
            ApiResult.Failure(AppError.Custom("이미지 업로드 실패"))
        }
    }
}
