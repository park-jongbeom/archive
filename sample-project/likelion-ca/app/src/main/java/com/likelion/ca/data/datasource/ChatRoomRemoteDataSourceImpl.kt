package com.likelion.ca.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.likelion.ca.data.model.ChatRoomDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRoomRemoteDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ChatRoomRemoteDataSource {
    private val collection = db.collection("chatrooms")

    override suspend fun getRooms(): List<ChatRoomDto> {
        val snapshot = collection.get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(ChatRoomDto::class.java)?.apply { id = doc.id }
        }
    }

    override suspend fun getRoom(roomId: String): ChatRoomDto? {
        val snapshot = collection.document(roomId).get().await()
        return snapshot.toObject(ChatRoomDto::class.java)?.apply { id = snapshot.id }
    }

    override suspend fun createRoom(room: ChatRoomDto) {
        collection.document(room.id).set(room).await()
    }

    override suspend fun updateRoom(room: ChatRoomDto) {
        collection.document(room.id).set(room, com.google.firebase.firestore.SetOptions.merge()).await()
    }
}
