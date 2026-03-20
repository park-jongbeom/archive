package com.likelion.liontalk

import android.app.Application
import com.likelion.liontalk.core.data.auth.AuthStateRepository
import com.kakao.sdk.common.KakaoSdk
import com.google.firebase.FirebaseApp
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class LionTalkApplication : Application() {
    @Inject
    lateinit var authStateRepository: AuthStateRepository

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        KakaoSdk.init(this,"")

        authStateRepository.start()

//        // 네이버는 반드시 application 레벨에서 초기화를 해줘야한다.
//        NaverIdLoginSDK.initialize(this,"naver_client_id","naver_client_secret","liontalk")
    }
}