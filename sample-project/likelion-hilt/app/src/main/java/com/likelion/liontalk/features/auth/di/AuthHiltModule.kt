package com.likelion.liontalk.features.auth.di

import android.content.Context
import com.likelion.liontalk.features.auth.data.AuthRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 인증(Authentication) 관련 의존성을 제공하는 Hilt 모듈입니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object AuthHiltModule {

    /**
     * 앱 전역 인증 레포지토리 인스턴스를 생성합니다.
     */
    @Provides
    @Singleton
    fun provideAuthRepository(
        @ApplicationContext context: Context
    ): AuthRepository = AuthRepository(context = context)
}

