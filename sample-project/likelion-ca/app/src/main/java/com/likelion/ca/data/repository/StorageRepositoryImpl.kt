package com.likelion.ca.data.repository

import com.likelion.ca.data.datasource.StorageRemoteDataSource
import com.likelion.ca.domain.repository.StorageRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRepositoryImpl @Inject constructor(
    private val storageRemoteDataSource: StorageRemoteDataSource
) : StorageRepository {

    override suspend fun getDownloadUrl(storagePath: String): String {
        return storageRemoteDataSource.getDownloadUrl(storagePath)
    }

    override suspend fun uploadChatImage(roomId: String, senderDisplayName: String, localImageUri: String): String {
        val fileName = "${System.currentTimeMillis()}_${UUID.randomUUID()}"
        val path = "chatrooms/$roomId/images/$senderDisplayName/$fileName"
        return storageRemoteDataSource.uploadFile(path, localImageUri)
    }
}
