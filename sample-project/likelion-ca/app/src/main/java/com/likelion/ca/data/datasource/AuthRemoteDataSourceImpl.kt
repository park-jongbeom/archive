package com.likelion.ca.data.datasource

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.functions.FirebaseFunctions
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.OAuthLoginCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class AuthRemoteDataSourceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFunctions: FirebaseFunctions
) : AuthRemoteDataSource {

    override suspend fun loginWithKakao(): String = suspendCancellableCoroutine { cont ->
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
            if (error != null) {
                if (cont.isActive) cont.resumeWithException(error)
            } else if (token != null) {
                if (cont.isActive) cont.resume(token.accessToken)
            } else {
                if (cont.isActive) cont.resumeWithException(Exception("Kakao login failed"))
            }
        }
    }

    override suspend fun loginWithNaver(): String = suspendCancellableCoroutine { cont ->
        NaverIdLoginSDK.authenticate(context, object : OAuthLoginCallback {
            override fun onSuccess() {
                val token = NaverIdLoginSDK.getAccessToken()
                if (token != null && cont.isActive) cont.resume(token)
                else if (cont.isActive) cont.resumeWithException(Exception("Naver token null"))
            }
            override fun onFailure(httpStatus: Int, message: String) {
                if (cont.isActive) cont.resumeWithException(Exception(message))
            }
            override fun onError(errorCode: Int, message: String) {
                if (cont.isActive) cont.resumeWithException(Exception(message))
            }
        })
    }

    override suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential).await()
    }

    override suspend fun signInWithCustomToken(functionName: String, accessToken: String) {
        val result = firebaseFunctions.getHttpsCallable(functionName)
            .call(mapOf("accessToken" to accessToken))
            .await()
        val customToken = ((result.data as? Map<*, *>)?.get("token") as? String)
            ?: throw Exception("Token not found in response")
        firebaseAuth.signInWithCustomToken(customToken).await()
    }

    override suspend fun signUpWithEmail(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await()
    }

    override suspend fun signInWithEmail(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password).await()
    }
}
