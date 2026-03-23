package com.likelion.liontalk.features.auth.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import kotlinx.coroutines.launch
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.likelion.liontalk.features.auth.viewmodel.SignNavigationEvent
import com.likelion.liontalk.features.auth.viewmodel.SignViewModel
import com.likelion.liontalk.core.ui.error.UiEvent
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
/**
 * 로그인 화면을 구성하고 인증 동작/결과 처리를 트리거하는 UI입니다.
 */
fun SignScreen(navController: NavController) {

    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val backStackEntry = navController.currentBackStackEntry ?: return
    val viewModel: SignViewModel = viewModel(viewModelStoreOwner = backStackEntry)

    var showDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("오류") }
    var dialogMessage by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        launch {
            viewModel.uiEvents.collect { event ->
                when (event) {
                    is UiEvent.ShowDialog -> {
                        dialogTitle = event.title
                        dialogMessage = event.message
                        showDialog = true
                    }
                    is UiEvent.ShowSnackbar -> {
                        // 로그인 화면은 Dialog 우선 정책이므로 snackbar은 기본 처리하지 않습니다.
                    }
                }
            }
        }
        launch {
            viewModel.navigationEvents.collect { event ->
                when (event) {
                    is SignNavigationEvent.NavigateTo -> {
                        navController.navigate(event.route) {
                            event.popUpRoute?.let { popUpTo(it) { inclusive = true } }
                        }
                    }
                }
            }
        }
    }

    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.let { intent -> viewModel.handleGoogleSignInResult(intent) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { viewModel.kakaoLogin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("카카오 로그인")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.naverLogin() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("네이버 로그인")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.googleLogin(context, googleLauncher) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("구글 로그인")
        }

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("이메일") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("비밀번호") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            // keyboardOptions는 현재 Compose 버전 호환성 때문에 생략
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.emailLogin(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("이메일 로그인")
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = { viewModel.emailSignUp(email, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("이메일 회원가입")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(dialogTitle) },
            text = { Text(dialogMessage) },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("확인")
                }
            }
        )
    }
}
