package com.likelion.ca.data.di

import com.likelion.ca.data.datasource.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    @Singleton
    abstract fun bindUserRemoteDataSource(impl: UserRemoteDataSourceImpl): UserRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindChatRoomRemoteDataSource(impl: ChatRoomRemoteDataSourceImpl): ChatRoomRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindAuthRemoteDataSource(impl: AuthRemoteDataSourceImpl): AuthRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindChatMessageRemoteDataSource(impl: ChatMessageRemoteDataSourceImpl): ChatMessageRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindStorageRemoteDataSource(impl: StorageRemoteDataSourceImpl): StorageRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindChatRoomEventRemoteDataSource(impl: ChatRoomEventRemoteDataSourceImpl): ChatRoomEventRemoteDataSource
}
