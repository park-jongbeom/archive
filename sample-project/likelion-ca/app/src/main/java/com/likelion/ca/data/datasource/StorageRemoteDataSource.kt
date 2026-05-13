package com.likelion.ca.data.datasource

/**
 * 이미지 및 파일 업로드 관련 원격 데이터 소스 인터페이스입니다.
 */
interface StorageRemoteDataSource {
    suspend fun uploadFile(path: String, localUri: String): String
    suspend fun getDownloadUrl(path: String): String
}
