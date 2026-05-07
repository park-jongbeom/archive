package com.likelion.ca.domain.repository

/**
 * [localImageUri]는 content:// 등 Android [android.net.Uri] 문자열 표현입니다.
 */
interface StorageRepository {
    suspend fun getDownloadUrl(storagePath: String): String
    suspend fun uploadChatImage(roomId: String, senderDisplayName: String, localImageUri: String): String
}
