package com.likelion.liontalk.core.data.users

import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.likelion.liontalk.core.model.ChatUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

/**
 * 사용자 프로필 정보를 Firestore에서 읽고/생성/수정하는 레포지토리입니다.
 */
class UserProfileRepository @Inject constructor() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collection = db.collection("users")

    /**
     * 로그인된 사용자를 기준으로 Firestore에 사용자 문서를 보장하고, 최신 프로필을 반환합니다.
     *
     * @param firebaseUser FirebaseAuth의 사용자 객체
     * @param provider 로그인 제공자 문자열(예: google/kakao/naver/email)
     */
    suspend fun ensureUser(firebaseUser: FirebaseUser, provider: String?): ChatUser {
        val uid = firebaseUser.uid
        val defaultName = firebaseUser.displayName ?: firebaseUser.email ?: uid
        val defaultAvatarUrl = firebaseUser.photoUrl?.toString()
        val providerValue = provider ?: detectProvider(firebaseUser)

        val snapshot = collection.document(uid).get().await()
        if (!snapshot.exists()) {
            val data = mapOf(
                "id" to uid,
                "name" to defaultName,
                "avatarUrl" to (defaultAvatarUrl ?: ""),
                "provider" to providerValue
            )
            collection.document(uid).set(data).await()
            return ChatUser(id = uid, name = defaultName, avatarUrl = defaultAvatarUrl)
        }

        // name이 비어있으면(초기 생성/마이그레이션) 보정
        val storedName = snapshot.getString("name").orEmpty()
        val storedAvatarUrl = snapshot.getString("avatarUrl")

        val nextName = storedName.takeIf { it.isNotBlank() } ?: defaultName
        val nextAvatarUrl = storedAvatarUrl?.takeIf { it.isNotBlank() } ?: defaultAvatarUrl
        val storedProvider = snapshot.getString("provider")

        if (nextName != storedName || nextAvatarUrl != (storedAvatarUrl?.takeIf { it.isNotBlank() })) {
            collection.document(uid).update(
                mapOf(
                    "name" to nextName,
                    "avatarUrl" to (nextAvatarUrl ?: "")
                )
            ).await()
        }

        // provider는 문서에 이미 있으면 유지, 비어있으면 채운다.
        if (storedProvider.isNullOrBlank() && providerValue.isNotBlank()) {
            collection.document(uid).update("provider", providerValue).await()
        }

        return ChatUser(
            id = uid,
            name = nextName,
            avatarUrl = nextAvatarUrl
        )
    }

    /**
     * 사용자 프로필 정보를 Firestore에 저장합니다.
     */
    suspend fun updateProfile(uid: String, name: String, avatarUrl: String?) {
        val safeName = name.takeIf { it.isNotBlank() } ?: ""
        val safeAvatarUrl = avatarUrl?.takeIf { it.isNotBlank() }
        collection.document(uid).set(
            mapOf(
                "id" to uid,
                "name" to safeName,
                "avatarUrl" to (safeAvatarUrl ?: ""),
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    private fun detectProvider(firebaseUser: FirebaseUser): String {
        val providerIds = firebaseUser.providerData.map { it.providerId }
        return when {
            providerIds.any { it.contains("google", ignoreCase = true) } -> "google"
            providerIds.any { it == "password" || it.contains("password", ignoreCase = true) } -> "email"
            else -> "email" // 커스텀 토큰 기반 로그인 등(providerData가 애매한 경우) 기본값
        }
    }
}

