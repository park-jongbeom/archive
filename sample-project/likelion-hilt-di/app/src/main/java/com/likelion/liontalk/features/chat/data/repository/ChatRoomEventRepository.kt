package com.likelion.liontalk.features.chat.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.likelion.liontalk.core.data.model.ChatUser
import com.likelion.liontalk.features.chat.data.model.RoomEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore의 채팅방 이벤트 컬렉션을 통해 이벤트를 실시간으로 수신하고 전송하는 레포지토리입니다.
 *
 * enter/leave/typing/lock 등 이벤트를 `listenEvents`로 구독하고, `sendEvent`로 발행합니다.
 */
@Singleton
class ChatRoomEventRepository @Inject constructor() {
    private companion object {
        private const val TAG = "ChatRoomEventRepository"
    }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * 특정 room의 이벤트(enter/leave/typing/lock 등)를 실시간으로 수신합니다.
     */
    fun listenEvents(roomId: String): Flow<List<RoomEvent>> = callbackFlow {
        val ref = db.collection("chatrooms")
            .document(roomId)
            .collection("events")
            .orderBy("timestamp")

        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(RoomEvent::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(list)
        }

        awaitClose { registration.remove() }
    }

    /**
     * 채팅방 이벤트를 서버에 전송합니다.
     */
    suspend fun sendEvent(
        roomId: String,
        type: String,
        sender: ChatUser,
        target: ChatUser? = null,
        typing: Boolean? = null
    ) {
        val doc = hashMapOf(
            "type" to type,
            // legacy fields for existing data/UI fallback
            "sender" to sender.name,
            // new nested payload for id/name/avatar display
            "senderUser" to hashMapOf(
                "id" to sender.id,
                "name" to sender.name,
                "avatarUrl" to (sender.avatarUrl ?: "")
            ),
            "timestamp" to System.currentTimeMillis()
        ).apply {
            if (target != null) {
                this["target"] = target.name
                this["targetUser"] = hashMapOf(
                    "id" to target.id,
                    "name" to target.name,
                    "avatarUrl" to (target.avatarUrl ?: "")
                )
            }
            if (typing != null) this["typing"] = typing
        }

        try {
            db.collection("chatrooms")
                .document(roomId)
                .collection("events")
                .add(doc)
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "sendEvent(roomId=$roomId, type=$type) failed", e)
            throw e
        }
    }
}

