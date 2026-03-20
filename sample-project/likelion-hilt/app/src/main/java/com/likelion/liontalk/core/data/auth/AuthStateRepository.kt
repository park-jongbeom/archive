package com.likelion.liontalk.core.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.likelion.liontalk.core.data.users.UserProfileRepository
import com.likelion.liontalk.core.model.ChatUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import android.util.Log
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 전역 인증 상태와 사용자 프로필(`me`)을 보관하고 동기화하는 레포지토리입니다.
 */
@Singleton
class AuthStateRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userProfileRepository: UserProfileRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * FirebaseAuth의 현재 인증 유저 상태를 노출합니다.
     */
    private val _authUser = MutableStateFlow<FirebaseUser?>(firebaseAuth.currentUser)
    val authUser: StateFlow<FirebaseUser?> = _authUser.asStateFlow()

    /**
     * 사용자 프로필(`me`) 상태를 노출합니다.
     */
    private val _me = MutableStateFlow<ChatUser?>(null)
    val me: StateFlow<ChatUser?> = _me.asStateFlow()

    /**
     * [me]의 현재 값을 동기적으로 가져옵니다.
     */
    val meOrNull: ChatUser? get() = _me.value

    private var started = false

    // uid -> in-flight Deferred
    private val inFlight: ConcurrentHashMap<String, Deferred<ChatUser>> = ConcurrentHashMap()
    private val mutex = Mutex()

    /**
     * FirebaseAuth 상태 변경을 구독하고, 인증이 갱신될 때 사용자 프로필도 준비합니다.
     *
     * 앱 시작 시 호출되도록 구성되어 있습니다.
     */
    fun start() {
        if (started) return
        started = true

        firebaseAuth.addAuthStateListener { auth ->
            _authUser.value = auth.currentUser
            scope.launch {
                try {
                    ensureUserProfile()
                } catch (e: Exception) {
                    Log.e("AuthStateRepository", "ensureProfileInitialized failed", e)
                    _me.value = null
                }
            }
        }
    }

    /**
     * 현재 로그인된 사용자를 기준으로 Firestore의 사용자 문서를 보장한 뒤 최신 프로필을 반환합니다.
     */
    suspend fun ensureUserProfile(provider: String? = null) {
        val currentUser = firebaseAuth.currentUser ?: return
        val uid = currentUser.uid

        val deferred = inFlight[uid] ?: mutex.withLock {
            inFlight[uid] ?: scope.async {
                userProfileRepository.ensureUser(currentUser, provider)
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

    /**
     * 로그아웃 처리 후 사용자 프로필 상태를 초기화합니다.
     */
    suspend fun logoutAndClearProfile() {
        val uid = _me.value?.id
        firebaseAuth.signOut()
        if (uid != null) inFlight.remove(uid)?.cancel() else {
            inFlight.forEach { (_, deferred) -> deferred.cancel() }
            inFlight.clear()
        }
        _me.value = null
    }

    /**
     * 현재 보관된 사용자 프로필이 없을 경우 예외를 발생시키며 [ChatUser]를 반환합니다.
     */
    fun requireMe(): ChatUser = requireNotNull(_me.value)
}

