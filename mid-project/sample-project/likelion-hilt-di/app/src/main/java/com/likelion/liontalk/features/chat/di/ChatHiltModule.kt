package com.likelion.liontalk.features.chat.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Chat 관련 Hilt Module.
 *
 * `com.likelion.liontalk.features.chat.data.repository`의 ChatRoomRepository,
 * ChatMessageRepository, ChatRoomEventRepository, 그리고 StorageRepository는
 * 각각 @Singleton @Inject constructor()로 선언되어 있어 Hilt가 자동으로 주입합니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object ChatHiltModule

