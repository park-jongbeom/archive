package com.likelion.ca.domain.result

import com.likelion.ca.domain.error.AppError

/**
 * 도메인/데이터 레이어에서 결과를 전달할 때 사용하는 래퍼 클래스입니다.
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Failure(val error: AppError) : ApiResult<Nothing>()
}

/**
 * ApiResult를 편리하게 처리하기 위한 확장 함수들입니다.
 */
inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onFailure(action: (AppError) -> Unit): ApiResult<T> {
    if (this is ApiResult.Failure) action(error)
    return this
}

fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> {
    return when (this) {
        is ApiResult.Success -> ApiResult.Success(transform(data))
        is ApiResult.Failure -> ApiResult.Failure(error)
    }
}
