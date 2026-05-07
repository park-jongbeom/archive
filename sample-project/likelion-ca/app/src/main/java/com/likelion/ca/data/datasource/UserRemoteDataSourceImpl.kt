package com.likelion.ca.data.datasource

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.likelion.ca.data.model.UserDto
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSourceImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val db: FirebaseFirestore
) : UserRemoteDataSource {
    private val usersCollection = db.collection("users")

    override val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    override fun addAuthStateListener(listener: (FirebaseAuth) -> Unit) {
        firebaseAuth.addAuthStateListener(listener)
    }

    override suspend fun getUserProfile(uid: String): UserDto? {
        val snapshot = usersCollection.document(uid).get().await()
        return snapshot.toObject(UserDto::class.java)
    }

    override suspend fun saveUserProfile(user: UserDto) {
        usersCollection.document(user.id).set(user, SetOptions.merge()).await()
    }

    override suspend fun updateProfile(uid: String, name: String, avatarUrl: String?) {
        usersCollection.document(uid).update(
            mapOf(
                "name" to name,
                "avatarUrl" to (avatarUrl ?: ""),
            )
        ).await()
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }
}
