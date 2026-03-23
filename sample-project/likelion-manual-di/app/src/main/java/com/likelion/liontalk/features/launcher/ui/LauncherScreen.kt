package com.likelion.liontalk.features.launcher.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.likelion.liontalk.core.navigation.Screen
import com.likelion.liontalk.core.ui.error.UiEvent
import com.likelion.liontalk.LionTalkApplication
import com.likelion.liontalk.core.di.ViewModelFactory
import com.likelion.liontalk.features.launcher.viewmodel.LauncherViewModel
import kotlinx.coroutines.delay

@Composable
/**
 * 앱 실행 직후 로딩/페이드 연출을 보여주고, 인증 상태에 따라 다음 화면으로 이동합니다.
 */
fun LauncherScreen(navHostController: NavHostController) {
    val backStackEntry = navHostController.currentBackStackEntry ?: return
    val context = LocalContext.current
    val appContainer = (context.applicationContext as LionTalkApplication).container
    val factory = remember(backStackEntry, appContainer) {
        ViewModelFactory(appContainer, backStackEntry, backStackEntry.arguments)
    }
    val viewModel: LauncherViewModel =
        viewModel(viewModelStoreOwner = backStackEntry, factory = factory)

    var alpha by remember { mutableStateOf(0f) }
    var fatalDialogMessage by remember { mutableStateOf<String?>(null) }
    var pendingRoute by remember { mutableStateOf<String?>(null) }

    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = 800),
        label = "fade-in"
    )

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowDialog -> {
                    fatalDialogMessage = event.message
                    pendingRoute = Screen.SignScreen.route
                }
                is UiEvent.ShowSnackbar -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(200)
        alpha = 1f

        delay(1500)
        val destination = viewModel.decideStartDestination()
        if (destination != null) {
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
            text = "LionTalk",
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier.alpha(animatedAlpha)
        )
    }

    if (fatalDialogMessage != null && pendingRoute != null) {
        AlertDialog(
            onDismissRequest = {
                fatalDialogMessage = null
                pendingRoute = null
            },
            title = { Text("오류") },
            text = { Text(fatalDialogMessage ?: "") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val route = pendingRoute ?: Screen.SignScreen.route
                        pendingRoute = null
                        fatalDialogMessage = null
                        navHostController.navigate(route) {
                            popUpTo(Screen.LauncherScreen.route) { inclusive = true }
                        }
                    }
                ) {
                    Text("확인")
                }
            }
        )
    }
}

