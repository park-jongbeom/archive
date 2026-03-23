package com.likelion.liontalk.core.data.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

/**
 * Firebase Storage 업로드를 앱 전역에서 재사용하기 위한 레포지토리입니다.
 */
class StorageRepository {
    private companion object {
        private const val TAG = "StorageRepository"
    }

    private val storage = FirebaseStorage.getInstance()

    /**
     * 버킷 루트 기준 경로(예: `chatrooms/{roomId}/images/...`)에 대한 HTTPS 다운로드 URL.
     * 이미 업로드된 객체 경로만 알 때 호출하면 됨.
     */
    suspend fun getDownloadUrl(storagePath: String): String {
        return try {
            storage.reference.child(storagePath).downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "getDownloadUrl failed: $storagePath", e)
            throw e
        }
    }

    /**
     * 채팅방 이미지 업로드 후 다운로드 URL 문자열을 반환합니다.
     */
    suspend fun uploadChatImage(roomId: String, sender: String, imageUri: Uri): String {
        val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}"
        val path = "chatrooms/$roomId/images/$sender/$fileName"

        val ref = storage.reference.child(path)
        val uploadTask = ref.putFile(imageUri)
        uploadTask.await()

        return getDownloadUrl(path)
    }
}
