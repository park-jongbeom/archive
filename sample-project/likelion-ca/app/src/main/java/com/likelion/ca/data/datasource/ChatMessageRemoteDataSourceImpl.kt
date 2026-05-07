package com.likelion.ca.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.likelion.ca.data.model.ChatMessageDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatMessageRemoteDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ChatMessageRemoteDataSource {

    override fun getMessagesFlow(roomId: String): Flow<List<ChatMessageDto>> = callbackFlow {
        val ref = db.collection("chatrooms")
            .document(roomId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ChatMessageDto::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(list)
        }

        awaitClose { registration.remove() }
    }

    override suspend fun sendMessage(message: ChatMessageDto): String {
        val messageCollection = db.collection("chatrooms")
            .document(message.roomId)
            .collection("messages")

        val docRef = messageCollection.add(message).await()
        val id = docRef.id
        messageCollection.document(id).update("id", id).await()
        return id
    }
}
