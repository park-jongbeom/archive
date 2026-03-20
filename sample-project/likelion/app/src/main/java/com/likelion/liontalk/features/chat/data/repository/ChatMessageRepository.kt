package com.likelion.liontalk2.features.chat.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.likelion.liontalk2.features.chat.data.model.ChatMessage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore의 `chatrooms/{roomId}/messages` 컬렉션을 대상으로 메시지를 수신/전송하는 레포지토리입니다.
 *
 * 특정 채팅방(roomId)의 메시지 실시간 스트림(Flow)을 제공하고, 메시지 전송을 처리합니다.
 */
@Singleton
class ChatMessageRepository @Inject constructor() {
    private companion object {
        private const val TAG = "ChatMessageRepository"
    }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * 특정 room의 메시지를 실시간으로 수신하는 스트림입니다.
     */
    fun getMessagesForRoomFlow(roomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val ref = db.collection("chatrooms")
            .document(roomId)
            .collection("messages")
            .orderBy("createdAt")

        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(ChatMessage::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(list)
        }

        awaitClose { registration.remove() }
    }

    /**
     * 전달받은 메시지를 서버에 저장하고, 저장된 메시를 반환합니다.
     */
    suspend fun sendMessage(message: ChatMessage): ChatMessage {
        val messageCollection = db.collection("chatrooms")
            .document(message.roomId)
            .collection("messages")

        val docRef = messageCollection.add(message).await()
        val id = docRef.id
        messageCollection.document(id).update("id", id).await()
        return message.copy(id = id)
    }
}
