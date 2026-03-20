package com.likelion.liontalk2.core.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.likelion.liontalk2.core.data.auth.AuthStateRepository
import com.likelion.liontalk2.core.data.storage.StorageRepository
import com.likelion.liontalk2.core.data.users.UserProfileRepository
import com.likelion.liontalk2.features.auth.data.AuthRepository
import com.likelion.liontalk2.features.chat.data.repository.ChatMessageRepository
import com.likelion.liontalk2.features.chat.data.repository.ChatRoomEventRepository
import com.likelion.liontalk2.features.chat.data.repository.ChatRoomRepository

/**
 * 레포지토리/공용 의존성을 수동으로 생성하기 위한 컨테이너입니다.
 */
class AppContainer(
    applicationContext: Context,
) {
    private val appContext: Context = applicationContext

    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    val userProfileRepository: UserProfileRepository by lazy { UserProfileRepository() }

    val authStateRepository: AuthStateRepository by lazy {
        AuthStateRepository(
            firebaseAuth = firebaseAuth,
            userProfileRepository = userProfileRepository,
        )
    }

    val authRepository: AuthRepository by lazy { AuthRepository(appContext) }

    val chatRoomRepository: ChatRoomRepository by lazy { ChatRoomRepository() }
    val chatMessageRepository: ChatMessageRepository by lazy { ChatMessageRepository() }
    val chatRoomEventRepository: ChatRoomEventRepository by lazy { ChatRoomEventRepository() }

    val storageRepository: StorageRepository by lazy { StorageRepository() }
}

