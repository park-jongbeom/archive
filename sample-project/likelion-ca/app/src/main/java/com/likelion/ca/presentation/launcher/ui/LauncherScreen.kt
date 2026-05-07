package com.likelion.ca.presentation.launcher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.likelion.ca.core.navigation.Screen
import com.likelion.ca.presentation.launcher.viewmodel.LauncherUiState
import com.likelion.ca.presentation.launcher.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay

@Composable
/**
 * 앱 실행 직후 로딩/페이드 연출을 보여주고, 인증 상태에 따라 다음 화면으로 이동합니다.
 */
fun LauncherScreen(navHostController: NavHostController) {
    val viewModel: LauncherViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var alpha by remember { mutableStateOf(0f) }
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 800),
        label = "fade-in"
    )

    LaunchedEffect(Unit) {
        delay(200)
        alpha = 1f
        delay(1500)
        viewModel.decideStartDestination()
    }

    LaunchedEffect(uiState) {
        if (uiState is LauncherUiState.Success) {
            val destination = (uiState as LauncherUiState.Success).destination
            navHostController.navigate(destination) {
                popUpTo(Screen.LauncherScreen.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Likelion CA",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.alpha(animatedAlpha)
        )
        
        if (uiState is LauncherUiState.Loading) {
            CircularProgressIndicator(color = Color.Gray)
        }
    }

    if (uiState is LauncherUiState.Error) {
        val message = (uiState as LauncherUiState.Error).message
        AlertDialog(
            onDismissRequest = { /* Force action */ },
            title = { Text("오류") },
            text = { Text(message) },
            confirmButton = {
                TextButton(
                    onClick = {
                        navHostController.navigate(Screen.SignScreen.route) {
                            popUpTo(Screen.LauncherScreen.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("로그인 화면으로 이동")
                }
            }
        )
    }
}
