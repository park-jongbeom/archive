package com.likelion.liontalk2.core.data.storage

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase Storage 업로드를 앱 전역에서 재사용하기 위한 레포지토리입니다.
 */
@Singleton
class StorageRepository @Inject constructor() {
    private companion object {
        private const val TAG = "StorageRepository"
    }

    private val storage = FirebaseStorage.getInstance()

    /**
     * 채팅방 이미지 업로드 후 다운로드 URL 문자열을 반환합니다.
     */
    suspend fun uploadChatImage(roomId: String, sender: String, imageUri: Uri): String {
        val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}"
        val path = "chatrooms/$roomId/images/$sender/$fileName"

        val ref = storage.reference.child(path)
        val uploadTask = ref.putFile(imageUri)
        uploadTask.await()

        return try {
            ref.downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "uploadChatImage downloadUrl failed", e)
            throw e
        }
    }
}

