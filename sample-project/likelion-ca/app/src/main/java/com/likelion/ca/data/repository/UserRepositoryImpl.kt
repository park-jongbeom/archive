package com.likelion.ca.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseUser
import com.likelion.ca.data.datasource.UserRemoteDataSource
import com.likelion.ca.data.mapper.toDomain
import com.likelion.ca.data.mapper.toDto
import com.likelion.ca.domain.model.AuthSession
import com.likelion.ca.domain.model.ChatUser
import com.likelion.ca.domain.repository.UserRepository
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
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
) : UserRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _authSession = MutableStateFlow(userRemoteDataSource.currentUser?.toAuthSession())
    override val authSession: StateFlow<AuthSession?> = _authSession.asStateFlow()

    private val _me = MutableStateFlow<ChatUser?>(null)
    override val me: StateFlow<ChatUser?> = _me.asStateFlow()

    override val meOrNull: ChatUser? get() = _me.value

    private var started = false
    private val inFlight: ConcurrentHashMap<String, Deferred<ChatUser>> = ConcurrentHashMap()
    private val mutex = Mutex()

    override fun start() {
        if (started) return
        started = true

        userRemoteDataSource.addAuthStateListener { auth ->
            _authSession.value = auth.currentUser?.toAuthSession()
            scope.launch {
                try {
                    ensureUserProfile()
                } catch (e: Exception) {
                    Log.e("UserRepositoryImpl", "ensureProfileInitialized failed", e)
                    _me.value = null
                }
            }
        }
    }

    override suspend fun ensureUserProfile(provider: String?) {
        val currentUser = userRemoteDataSource.currentUser ?: return
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

    override suspend fun logoutAndClearProfile() {
        val uid = _me.value?.id
        userRemoteDataSource.signOut()
        if (uid != null) inFlight.remove(uid)?.cancel() else {
            inFlight.forEach { (_, deferred) -> deferred.cancel() }
            inFlight.clear()
        }
        _me.value = null
    }

    override fun requireMe(): ChatUser = requireNotNull(_me.value)

    override suspend fun updateProfile(uid: String, name: String, avatarUrl: String?) {
        userRemoteDataSource.updateProfile(uid, name, avatarUrl)
    }

    private suspend fun ensureUser(firebaseUser: FirebaseUser, provider: String?): ChatUser {
        val uid = firebaseUser.uid
        val defaultName = firebaseUser.displayName ?: firebaseUser.email ?: uid
        val defaultAvatarUrl = firebaseUser.photoUrl?.toString()
        val providerValue = provider ?: detectProvider(firebaseUser)

        val existingDto = userRemoteDataSource.getUserProfile(uid)
        if (existingDto == null) {
            val newUser = ChatUser(id = uid, name = defaultName, avatarUrl = defaultAvatarUrl)
            userRemoteDataSource.saveUserProfile(newUser.toDto(providerValue))
            return newUser
        }

        val storedUser = existingDto.toDomain()
        val nextName = storedUser.name.takeIf { it.isNotBlank() } ?: defaultName
        val nextAvatarUrl = storedUser.avatarUrl?.takeIf { it.isNotBlank() } ?: defaultAvatarUrl
        val storedProvider = existingDto.provider

        if (nextName != storedUser.name || nextAvatarUrl != storedUser.avatarUrl) {
            userRemoteDataSource.updateProfile(uid, nextName, nextAvatarUrl)
        }

        if (storedProvider.isBlank() && providerValue.isNotBlank()) {
            val updatedDto = existingDto.copy(provider = providerValue)
            userRemoteDataSource.saveUserProfile(updatedDto)
        }

        return ChatUser(
            id = uid,
            name = nextName,
            avatarUrl = nextAvatarUrl,
        )
    }

    private fun detectProvider(firebaseUser: FirebaseUser): String {
        val providerIds = firebaseUser.providerData.map { it.providerId }
        return when {
            providerIds.any { it.contains("google", ignoreCase = true) } -> "google"
            providerIds.any { it == "password" || it.contains("password", ignoreCase = true) } -> "email"
            else -> "email"
        }
    }

    private fun FirebaseUser.toAuthSession(): AuthSession = AuthSession(
        uid = uid,
        email = email,
        displayName = displayName,
    )
}
