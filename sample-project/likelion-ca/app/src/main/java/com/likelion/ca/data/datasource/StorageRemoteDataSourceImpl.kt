package com.likelion.ca.data.datasource

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageRemoteDataSourceImpl @Inject constructor(
    private val storage: FirebaseStorage
) : StorageRemoteDataSource {

    override suspend fun uploadFile(path: String, localUri: String): String {
        val uri = Uri.parse(localUri)
        val ref = storage.reference.child(path)
        ref.putFile(uri).await()
        return getDownloadUrl(path)
    }

    override suspend fun getDownloadUrl(path: String): String {
        return storage.reference.child(path).downloadUrl.await().toString()
    }
}
