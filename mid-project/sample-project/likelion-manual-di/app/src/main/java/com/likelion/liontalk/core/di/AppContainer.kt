package com.likelion.liontalk.core.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.likelion.liontalk.core.data.repository.StorageRepository
import com.likelion.liontalk.core.data.repository.UserRepository
import com.likelion.liontalk.features.auth.data.AuthRepository
import com.likelion.liontalk.features.chat.data.repository.ChatMessageRepository
import com.likelion.liontalk.features.chat.data.repository.ChatRoomEventRepository
import com.likelion.liontalk.features.chat.data.repository.ChatRoomRepository

/**
 * 레포지토리/공용 의존성을 수동으로 생성하기 위한 컨테이너입니다.
 */
class AppContainer(
    applicationContext: Context,
) {
    private val appContext: Context = applicationContext

    val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    val userRepository: UserRepository by lazy {
        UserRepository(firebaseAuth = firebaseAuth)
    }

    val authRepository: AuthRepository by lazy { AuthRepository(appContext) }

    val chatRoomRepository: ChatRoomRepository by lazy { ChatRoomRepository() }
    val chatMessageRepository: ChatMessageRepository by lazy { ChatMessageRepository() }
    val chatRoomEventRepository: ChatRoomEventRepository by lazy { ChatRoomEventRepository() }

    val storageRepository: StorageRepository by lazy { StorageRepository() }
}
