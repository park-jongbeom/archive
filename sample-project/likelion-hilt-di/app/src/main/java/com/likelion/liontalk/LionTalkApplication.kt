package com.likelion.liontalk

import android.app.Application
import com.likelion.liontalk.core.data.repository.UserRepository
import com.kakao.sdk.common.KakaoSdk
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LionTalkApplication : Application() {
    @Inject
    lateinit var userRepository: UserRepository

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        KakaoSdk.init(this,"")

        userRepository.start()

//        // 네이버는 반드시 application 레벨에서 초기화를 해줘야한다.
//        NaverIdLoginSDK.initialize(this,"naver_client_id","naver_client_secret","liontalk")
    }
}
