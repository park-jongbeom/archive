package com.likelion.ca.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.likelion.ca.data.model.RoomEventDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRoomEventRemoteDataSourceImpl @Inject constructor(
    private val db: FirebaseFirestore
) : ChatRoomEventRemoteDataSource {

    override fun listenEvents(roomId: String): Flow<List<RoomEventDto>> = callbackFlow {
        val ref = db.collection("chatrooms")
            .document(roomId)
            .collection("events")
            .orderBy("timestamp", Query.Direction.ASCENDING)

        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            val list = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(RoomEventDto::class.java)?.copy(id = doc.id)
            } ?: emptyList()

            trySend(list)
        }

        awaitClose { registration.remove() }
    }

    override suspend fun sendEvent(roomId: String, event: RoomEventDto) {
        db.collection("chatrooms")
            .document(roomId)
            .collection("events")
            .add(event)
            .await()
    }
}
