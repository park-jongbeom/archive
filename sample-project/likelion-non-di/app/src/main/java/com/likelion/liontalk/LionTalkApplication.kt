package com.likelion.liontalk

import android.app.Application
import android.content.Context
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import com.likelion.liontalk.core.data.repository.UserRepository

class LionTalkApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        FirebaseApp.initializeApp(this)
        KakaoSdk.init(this, "")

        UserRepository.start()

//        // 네이버는 반드시 application 레벨에서 초기화를 해줘야한다.
//        NaverIdLoginSDK.initialize(this,"naver_client_id","naver_client_secret","liontalk")
    }

    companion object {
        lateinit var appContext: Context
            private set
    }
}
