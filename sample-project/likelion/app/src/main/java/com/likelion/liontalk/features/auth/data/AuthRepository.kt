package com.likelion.liontalk2.features.auth.data

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import android.content.Intent
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.ktx.Firebase
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

/**
 * 다양한 로그인 제공자(카카오/네이버/구글/이메일) 인증 흐름을 처리하는 레포지토리입니다.
 *
 * 외부 제공자 토큰을 서버 함수를 통해 커스텀 토큰으로 교환한 뒤 Firebase 인증을 완료합니다.
 */
class AuthRepository(
    private val context: Context,
) {
    private companion object {
        private const val TAG = "AuthRepository"
    }

    /**
     * 카카오 로그인 후 Firebase 인증을 완료합니다.
     */
    suspend fun kakaoLogin(): Unit {
        val accessToken = signInWithKakao()
        signInWithCustomToken("kakaoCustomAuth", accessToken)
    }

    /**
     * 네이버 로그인 후 Firebase 인증을 완료합니다.
     */
    suspend fun naverLogin() {
        val accessToken = signInWithNaver()
        signInWithCustomToken("naverCustomAuth", accessToken)
    }

    /**
     * 구글 로그인 결과로부터 idToken을 추출해 Firebase 인증을 수행합니다.
     */
    suspend fun handleGoogleSignInResult(data: Intent?): AuthResult {
        requireNotNull(data) { "Google sign-in intent is null" }

        return try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(data)
                .getResult(ApiException::class.java)

            val idToken = account.idToken ?: error("idToken is null. " +
                    "requestIdToken(webClientId) 호출/웹 클라이언트 ID 확인 필요")
            signInWithGoogleIdToken(idToken)

        } catch (e: ApiException) {
            Log.e(TAG, "handleGoogleSignInResult failed code=${e.statusCode}", e)
            throw e
        }
    }

    private suspend fun signInWithCustomToken(functionName: String, accessToken: String) {
        val functions = Firebase.functions
        val auth = Firebase.auth

        val result = functions.getHttpsCallable(functionName)
            .call(mapOf("accessToken" to accessToken))
            .await()
        val customToken = ((result.data as? Map<*, *>)?.get("token") as? String)
            ?: error("서버 응답에서 토큰을 찾을 수 없습니다. (function=$functionName)")
        try {
            auth.signInWithCustomToken(customToken).await()
        } catch (e: Exception) {
            Log.e(TAG, "signInWithCustomToken(function=$functionName) failed", e)
            throw e
        }
    }

    private suspend fun signInWithGoogleIdToken(idToken: String): AuthResult {
        val auth = Firebase.auth
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        return auth.signInWithCredential(credential).await()
    }

    private suspend fun signInWithKakao(): String =
        suspendCancellableCoroutine { cont ->
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    if (cont.isActive) cont.resumeWithException(error)
                } else if (token != null) {
                    if (cont.isActive) cont.resume(token.accessToken)
                } else {
                    if (cont.isActive) cont.resumeWithException(
                        Exception("Kakao login failed: unknown error")
                    )
                }
            }
        }

    private suspend fun signInWithNaver(): String =
        suspendCancellableCoroutine { cont ->
            NaverIdLoginSDK.authenticate(context, object : OAuthLoginCallback {
                override fun onSuccess() {
                    val naverAccessToken = NaverIdLoginSDK.getAccessToken()
                    if (naverAccessToken != null) {
                        if (cont.isActive) cont.resume(naverAccessToken)
                    } else {
                        if (cont.isActive) cont.resumeWithException(
                            Exception("AccessToken is null")
                        )
                    }
                }

                override fun onFailure(httpStatus: Int, message: String) {
                    if (cont.isActive) cont.resumeWithException(
                        Exception("Login failed: $message")
                    )
                }

                override fun onError(errorCode: Int, message: String) {
                    if (cont.isActive) cont.resumeWithException(
                        Exception("Error $errorCode: $message")
                    )
                }
            })
        }

    /**
     * 이메일/비밀번호로 회원가입합니다.
     */
    suspend fun signUpWithEmail(email: String, password: String) {
        Firebase.auth.createUserWithEmailAndPassword(email, password).await()
    }

    /**
     * 이메일/비밀번호로 로그인합니다.
     */
    suspend fun signInWithEmail(email: String, password: String) {
        Firebase.auth.signInWithEmailAndPassword(email, password).await()
    }


}
