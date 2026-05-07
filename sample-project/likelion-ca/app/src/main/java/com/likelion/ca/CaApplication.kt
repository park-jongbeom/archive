package com.likelion.ca

import android.app.Application
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import com.likelion.ca.domain.repository.UserRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class CaApplication : Application() {
    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        KakaoSdk.init(this, "")

        userRepository.start()
    }
}
