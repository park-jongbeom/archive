package com.likelion.ca.domain.di

import com.likelion.ca.domain.repository.*
import com.likelion.ca.domain.usecase.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideComputeStartDestination(repo: UserRepository) = ComputeStartDestinationUseCase(repo)

    @Provides
    @Singleton
    fun provideLoginUseCase(auth: AuthRepository, user: UserRepository) = LoginUseCase(auth, user)

    @Provides
    @Singleton
    fun provideLogoutUseCase(user: UserRepository) = LogoutUseCase(user)

    @Provides
    @Singleton
    fun provideGetChatRoomsUseCase(room: ChatRoomRepository, user: UserRepository) = GetChatRoomsUseCase(room, user)

    @Provides
    @Singleton
    fun provideEnterChatRoomUseCase(room: ChatRoomRepository, user: UserRepository) = EnterChatRoomUseCase(room, user)

    @Provides
    @Singleton
    fun provideSendMessageUseCase(msg: ChatMessageRepository, user: UserRepository) = SendMessageUseCase(msg, user)

    @Provides
    @Singleton
    fun provideGetMessagesUseCase(msg: ChatMessageRepository) = GetMessagesUseCase(msg)

    @Provides
    @Singleton
    fun provideListenRoomEventsUseCase(evt: ChatRoomEventRepository) = ListenRoomEventsUseCase(evt)

    @Provides
    @Singleton
    fun provideSendRoomEventUseCase(evt: ChatRoomEventRepository, user: UserRepository) = SendRoomEventUseCase(evt, user)

    @Provides
    @Singleton
    fun provideUploadChatImageUseCase(storage: StorageRepository, user: UserRepository) = UploadChatImageUseCase(storage, user)

    @Provides
    @Singleton
    fun provideLeaveRoomUseCase(room: ChatRoomRepository, evt: ChatRoomEventRepository, user: UserRepository) = LeaveRoomUseCase(room, evt, user)

    @Provides
    @Singleton
    fun provideKickUserUseCase(room: ChatRoomRepository, evt: ChatRoomEventRepository, user: UserRepository) = KickUserUseCase(room, evt, user)
}
