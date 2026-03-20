package com.likelion.liontalk2

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import com.google.firebase.FirebaseApp

import com.likelion.liontalk2.core.di.AppContainer

class LionTalkApplication : Application() {
    val container: AppContainer by lazy { AppContainer(applicationContext) }

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        KakaoSdk.init(this, "")

        container.authStateRepository.start()

//        // 네이버는 반드시 application 레벨에서 초기화를 해줘야한다.
//        NaverIdLoginSDK.initialize(this,"naver_client_id","naver_client_secret","liontalk")
    }
}