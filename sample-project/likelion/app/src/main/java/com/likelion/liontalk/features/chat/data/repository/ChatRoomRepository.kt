package com.likelion.liontalk2.features.chat.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.likelion.liontalk2.core.model.ChatUser
import com.likelion.liontalk2.features.chat.data.model.ChatRoom
import com.likelion.liontalk2.features.chat.data.model.addUserIfNotExists
import com.likelion.liontalk2.features.chat.data.model.removeUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firestore의 `chatrooms` 컬렉션을 기반으로 채팅방 데이터를 조회하고 처리하는 레포지토리입니다.
 *
 * 채팅방 목록 조회/생성/삭제, 입장/퇴장, 잠금 상태 변경을 담당합니다.
 */
@Singleton
class ChatRoomRepository @Inject constructor() {
    private companion object {
        private const val TAG = "ChatRoomRepository"
    }

    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collection = db.collection("chatrooms")

    /**
     * 서버의 채팅방 목록을 1회 조회합니다.
     */
    suspend fun fetchRoomsOnce(): List<ChatRoom> {
        try {
            val snapshot = collection.get().await()
            return snapshot.documents.mapNotNull { doc ->
                doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            Log.e(TAG, "fetchRoomsOnce failed", e)
            throw Exception("서버 채팅방 목록 조회 실패: ${e.message}")
        }
    }

    /**
     * 새로운 채팅방을 생성합니다.
     */
    suspend fun createChatRoom(chatRoom: ChatRoom) {
        try {
            val docRef = collection.add(chatRoom).await()
            val id = docRef.id
            docRef.update("id", id).await()
        } catch (e: Exception) {
            Log.e(TAG, "createChatRoom failed", e)
            throw Exception("서버 채팅방 생성 실패: ${e.message}")
        }
    }

    /**
     * 지정한 roomId의 채팅방을 서버에서 삭제합니다.
     */
    suspend fun deleteChatRoomToRemote(roomId: String) {
        try {
            val querySnapshot = collection.whereEqualTo("id", roomId).limit(1).get().await()
            val doc = querySnapshot.documents.firstOrNull() ?: throw Exception("삭제할 채팅방 없음")
            collection.document(doc.id).delete().await()
        } catch (e: Exception) {
            Log.e(TAG, "deleteChatRoomToRemote failed", e)
            throw Exception("서버 채팅방 삭제 실패: ${e.message}")
        }
    }

    /**
     * 사용자가 채팅방에 입장하도록 처리하고, 업데이트된 채팅방 정보를 반환합니다.
     */
    suspend fun enterRoom(user: ChatUser, roomId: String): ChatRoom {
        val remoteRoom = fetchRoom(roomId)
            ?: throw Exception("서버 입장 처리 실패: room not found (roomId=$roomId)")

        return updateRoom(remoteRoom.addUserIfNotExists(user))
    }

    /**
     * 지정한 roomId의 채팅방 정보를 `Flow`로 제공합니다.
     */
    fun getChatRoomFlow(roomId: String): Flow<ChatRoom> = flow {
        emit(getRoomFromRemote(roomId) ?: throw Exception("해당 채팅방이 없습니다. (roomId=$roomId)"))
    }

    /**
     * 지정한 roomId의 채팅방을 서버에서 조회합니다.
     */
    suspend fun getRoomFromRemote(roomId: String): ChatRoom? = fetchRoom(roomId)

    /**
     * 사용자를 채팅방에서 제거(퇴장)합니다.
     */
    suspend fun removeUserFromRoom(user: ChatUser, roomId: String) {
        val room = fetchRoom(roomId) ?: throw Exception("퇴장실패 : 채팅방 정보가 없습니다. (roomId=$roomId)")
        updateRoom(room.removeUser(user))
    }

    /**
     * 채팅방의 잠금 상태를 변경합니다.
     */
    suspend fun toggleLock(isLock: Boolean, roomId: String) {
        val room = fetchRoom(roomId) ?: return
        updateRoom(room.copy(isLocked = isLock))
    }

    private suspend fun fetchRoom(id: String): ChatRoom? {
        try {
            val snapshot = collection.whereEqualTo("id", id)
                .limit(1)
                .get()
                .await()
            val doc = snapshot.documents.firstOrNull() ?: return null
            return doc.toObject(ChatRoom::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            Log.e(TAG, "fetchRoom(id=$id) failed", e)
            throw Exception("서버 채팅방 조회 실패: ${e.message}")
        }
    }

    private suspend fun updateRoom(room: ChatRoom): ChatRoom {
        try {
            val querySnapshot = collection.whereEqualTo("id", room.id).limit(1).get().await()
            val doc = querySnapshot.documents.firstOrNull() ?: throw Exception("채팅방 없음")
            collection.document(doc.id).set(room).await()
            return room
        } catch (e: Exception) {
            Log.e(TAG, "updateRoom(room=${room.id}) failed", e)
            throw Exception("서버 채팅방 변경 실패: ${e.message}")
        }
    }
}

