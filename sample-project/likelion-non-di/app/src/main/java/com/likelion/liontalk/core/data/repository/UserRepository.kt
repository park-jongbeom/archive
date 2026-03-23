package com.likelion.liontalk.core.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.likelion.liontalk.core.data.model.ChatUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import java.util.concurrent.ConcurrentHashMap

/** Auth 상태랑 Firestore 유저 문서 둘 다 여기서 맞춤. authUser는 Firebase 쪽, me는 앱에서 쓰는 프로필. */
object UserRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    private val _authUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)

    /** 지금 Firebase에 로그인돼 있으면 그 유저, 아니면 null */
    val authUser: StateFlow<FirebaseUser?> = _authUser.asStateFlow()

    private val _me = MutableStateFlow<ChatUser?>(null)

    /** Firestore 기준으로 맞춰 둔 내 프로필. 로그아웃하면 null */
    val me: StateFlow<ChatUser?> = _me.asStateFlow()

    /** Flow 말고 지금 값만 필요할 때 */
    val meOrNull: ChatUser? get() = _me.value

    private var started = false

    /** 같은 uid로 ensure 여러 번 불러도 Firestore는 한 번만 타게 묶어둠 */
    private val inFlight: ConcurrentHashMap<String, Deferred<ChatUser>> = ConcurrentHashMap()

    /** inFlight 넣을 때 동시에 두 군데서 만들지 않게 */
    private val mutex = Mutex()

    /** 앱 시작할 때 한 번. Auth 리스너 달고 로그인 상태 바뀌면 ensureUserProfile */
    fun start() {
        if (started) return
        started = true

        firebaseAuth.addAuthStateListener { auth ->
            _authUser.value = auth.currentUser
            scope.launch {
                try {
                    ensureUserProfile()
                } catch (e: Exception) {
                    Log.e("UserRepository", "ensureProfileInitialized failed", e)
                    _me.value = null
                }
            }
        }
    }

    /**
     * users 문서 없으면 만들고, 있으면 읽어서 me 갱신.
     * provider는 로그인 직후 넘기면 그대로 쓰고, null이면 FirebaseAuth의 providerData로 판별.
     */
    suspend fun ensureUserProfile(provider: String? = null) {
        val currentUser = firebaseAuth.currentUser ?: return
        val uid = currentUser.uid

        val deferred = inFlight[uid] ?: mutex.withLock {
            inFlight[uid] ?: scope.async {
                ensureUser(currentUser, provider)
            }.also { created ->
                inFlight[uid] = created
            }
        }

        val ensured = try {
            deferred.await()
        } finally {
            mutex.withLock {
                val current = inFlight[uid]
                if (current == deferred) {
                    inFlight.remove(uid)
                }
            }
        }

        _me.value = ensured
    }

    /** signOut + me 비우고, 돌고 있던 ensure 있으면 취소 */
    suspend fun logoutAndClearProfile() {
        val uid = _me.value?.id
        firebaseAuth.signOut()
        if (uid != null) inFlight.remove(uid)?.cancel() else {
            inFlight.forEach { (_, deferred) -> deferred.cancel() }
            inFlight.clear()
        }
        _me.value = null
    }

    /** me 없으면 그냥 터짐(requireNotNull) */
    fun requireMe(): ChatUser = requireNotNull(_me.value)

    /** 설정 화면에서 이름/아바타 저장할 때. merge라 기존 필드랑 합쳐짐 */
    suspend fun updateProfile(uid: String, name: String, avatarUrl: String?) {
        val safeName = name.takeIf { it.isNotBlank() } ?: ""
        val safeAvatarUrl = avatarUrl?.takeIf { it.isNotBlank() }
        usersCollection.document(uid).set(
            mapOf(
                "id" to uid,
                "name" to safeName,
                "avatarUrl" to (safeAvatarUrl ?: ""),
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).await()
    }

    /** users/{uid} get → 없으면 set, 있으면 비어있는 필드만 보정 */
    private suspend fun ensureUser(firebaseUser: FirebaseUser, provider: String?): ChatUser {
        val uid = firebaseUser.uid
        val defaultName = firebaseUser.displayName ?: firebaseUser.email ?: uid
        val defaultAvatarUrl = firebaseUser.photoUrl?.toString()
        val providerValue = provider ?: detectProvider(firebaseUser)

        val snapshot = usersCollection.document(uid).get().await()
        if (!snapshot.exists()) {
            val data = mapOf(
                "id" to uid,
                "name" to defaultName,
                "avatarUrl" to (defaultAvatarUrl ?: ""),
                "provider" to providerValue
            )
            usersCollection.document(uid).set(data).await()
            return ChatUser(id = uid, name = defaultName, avatarUrl = defaultAvatarUrl)
        }

        val storedName = snapshot.getString("name").orEmpty()
        val storedAvatarUrl = snapshot.getString("avatarUrl")

        val nextName = storedName.takeIf { it.isNotBlank() } ?: defaultName
        val nextAvatarUrl = storedAvatarUrl?.takeIf { it.isNotBlank() } ?: defaultAvatarUrl
        val storedProvider = snapshot.getString("provider")

        if (nextName != storedName || nextAvatarUrl != (storedAvatarUrl?.takeIf { it.isNotBlank() })) {
            usersCollection.document(uid).update(
                mapOf(
                    "name" to nextName,
                    "avatarUrl" to (nextAvatarUrl ?: "")
                )
            ).await()
        }

        if (storedProvider.isNullOrBlank() && providerValue.isNotBlank()) {
            usersCollection.document(uid).update("provider", providerValue).await()
        }

        return ChatUser(
            id = uid,
            name = nextName,
            avatarUrl = nextAvatarUrl
        )
    }

    /** providerData 보고 google / email 정도로만 구분 */
    private fun detectProvider(firebaseUser: FirebaseUser): String {
        val providerIds = firebaseUser.providerData.map { it.providerId }
        return when {
            providerIds.any { it.contains("google", ignoreCase = true) } -> "google"
            providerIds.any { it == "password" || it.contains("password", ignoreCase = true) } -> "email"
            else -> "email"
        }
    }
}
