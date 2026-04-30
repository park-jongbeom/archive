package com.likelion.liontalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.likelion.liontalk.core.navigation.ChatAppNavigation
import com.likelion.liontalk.LionTalkApplication

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            val app = application as LionTalkApplication
            ChatAppNavigation(
                navController = navController,
                userRepository = app.container.userRepository,
            )
        }
    }
}