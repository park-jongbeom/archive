package com.likelion.liontalk.core.di

import com.google.firebase.auth.FirebaseAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * FirebaseAuth 인스턴스를 앱 전역 단위에서 제공하기 위한 Hilt 모듈입니다.
 */
@Module
@InstallIn(SingletonComponent::class)
object FirebaseAuthHiltModule {

    /**
     * FirebaseAuth 싱글톤 인스턴스를 반환합니다.
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}

