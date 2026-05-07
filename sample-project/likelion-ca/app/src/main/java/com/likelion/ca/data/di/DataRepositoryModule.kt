package com.likelion.ca.data.di

import com.likelion.ca.data.repository.AuthRepositoryImpl
import com.likelion.ca.data.repository.ChatMessageRepositoryImpl
import com.likelion.ca.data.repository.ChatRoomEventRepositoryImpl
import com.likelion.ca.data.repository.ChatRoomRepositoryImpl
import com.likelion.ca.data.repository.StorageRepositoryImpl
import com.likelion.ca.data.repository.UserRepositoryImpl
import com.likelion.ca.domain.repository.AuthRepository
import com.likelion.ca.domain.repository.ChatMessageRepository
import com.likelion.ca.domain.repository.ChatRoomEventRepository
import com.likelion.ca.domain.repository.ChatRoomRepository
import com.likelion.ca.domain.repository.StorageRepository
import com.likelion.ca.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindChatRoomRepository(impl: ChatRoomRepositoryImpl): ChatRoomRepository

    @Binds
    @Singleton
    abstract fun bindChatMessageRepository(impl: ChatMessageRepositoryImpl): ChatMessageRepository

    @Binds
    @Singleton
    abstract fun bindChatRoomEventRepository(impl: ChatRoomEventRepositoryImpl): ChatRoomEventRepository

    @Binds
    @Singleton
    abstract fun bindStorageRepository(impl: StorageRepositoryImpl): StorageRepository
}
