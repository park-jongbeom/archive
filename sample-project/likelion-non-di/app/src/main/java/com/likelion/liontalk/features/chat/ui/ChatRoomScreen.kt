package com.likelion.liontalk.features.chat.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.likelion.liontalk.core.data.model.ChatUser
import com.likelion.liontalk.core.util.SoundPlayer
import com.likelion.liontalk.core.util.SoundType
import com.likelion.liontalk.core.ui.error.UiEvent
import com.likelion.liontalk.features.chat.ui.components.ChatMessageItem
import com.likelion.liontalk.features.chat.ui.components.ChatRoomSettingContent
import com.likelion.liontalk.features.chat.ui.components.ExplosionEffect
import com.likelion.liontalk.features.chat.data.model.isSameUser
import com.likelion.liontalk.features.chat.viewmodel.ChatRoomViewModel
import com.likelion.liontalk.features.chat.viewmodel.ChatRoomViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * 특정 roomId의 채팅방을 표시하고 메시지/이벤트 전송을 처리하는 화면입니다.
 */
fun ChatRoomScreen(navController: NavController, roomId: String){
    val context = LocalContext.current
    val backStackEntry = navController.currentBackStackEntry ?: return
    val factory = remember(backStackEntry) {
        ChatRoomViewModelFactory(backStackEntry, backStackEntry.arguments)
    }
    val viewModel: ChatRoomViewModel =
        viewModel(viewModelStoreOwner = backStackEntry, factory = factory)

    val messages by viewModel.messages.collectAsState()
    val inputMessage = remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.sendImage(it, inputMessage.value)
        }
    }

    val keyboardController = LocalSoftwareKeyboardController.current

    val typingUser = remember { mutableStateOf<ChatUser?>(null) }
    val eventFlow = viewModel.event
    var showLeaveDialog by remember { mutableStateOf(false) }

    var showSettingsPanel by remember { mutableStateOf(false)}

    var showKickDialog by remember { mutableStateOf(false) }
    var kickTarget by remember { mutableStateOf<ChatUser?>(null)}

    var showExplodDialog by remember { mutableStateOf(false) }
    val explodedState by viewModel.explodeState.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val soundPlayer = remember(context) { SoundPlayer(context) }
    fun playSentSound(soundtype: SoundType) {
        soundPlayer.play(soundtype)
    }

    LaunchedEffect(Unit) {
        eventFlow.collectLatest { event ->
            when(event) {
                is ChatRoomEvent.TypingStarted -> {
                    typingUser.value = event.sender
                }
                is ChatRoomEvent.TypingStopped -> {
                    typingUser.value = null
                }
                is ChatRoomEvent.ChatRoomEnter -> {
                    playSentSound(SoundType.ENTER_ROOM)
                }
                is ChatRoomEvent.ChatRoomLeave -> {
                }
                is ChatRoomEvent.ScrollToBottom -> {
                    coroutineScope.launch {
                        if(messages.isNotEmpty()) {

                            listState.animateScrollToItem(messages.lastIndex)
                        }
                    }
                }
                is ChatRoomEvent.ClearInput -> {
                    inputMessage.value = ""
                    keyboardController?.hide()
                }
                is ChatRoomEvent.Kicked -> {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = "채팅방에서 추방 당했습니다. ㅠ.ㅠ",
                            duration = SnackbarDuration.Short
                        )
                        navController.popBackStack()
                    }
                }
                is ChatRoomEvent.Exploded -> {
                    navController.previousBackStackEntry?.
                    savedStateHandle?.set("explodedRoomId",roomId)

                    navController.popBackStack()
                }
                else -> Unit
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(
                    message = event.message,
                    duration = SnackbarDuration.Short
                )
                is UiEvent.ShowDialog -> Unit
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("채팅방 #$roomId")
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.back {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            showSettingsPanel = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "설정"
                            )
                        }
                    }

                )
            },
            content = { padding ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding)
                        .navigationBarsPadding()
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                            .padding(8.dp),
                        state = listState

                    ) {
                        items(messages) { message ->
                            ChatMessageItem(message, viewModel.me?.isSameUser(message.sender) == true)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp)
                    ) {

                        if (typingUser.value != null) {
                            Text(
                                text = "${typingUser.value?.name}님이 입력중...",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                            )
                        }

                        OutlinedTextField(
                            value = inputMessage.value,
                            onValueChange = {
                                inputMessage.value = it
                                viewModel.onTypingChanged(it)
                                if (it.isBlank()) {
                                    viewModel.stopTyping()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                            placeholder = { Text("메세지 입력") },
                            maxLines = 4
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { imagePickerLauncher.launch("image/*") },
                            modifier = Modifier.height(56.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp)
                        ) {
                            Text("이미지")
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Button(
                            onClick = {
                                if (inputMessage.value.isNotBlank()) {
                                    viewModel.sendMessage(inputMessage.value)
                                }
                            },
                            modifier = Modifier
                                .height(56.dp),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "전송")
                        }
                    }
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        )

        AnimatedVisibility(
            visible = showSettingsPanel,
            enter = slideInHorizontally(initialOffsetX = { it}) + EnterTransition.None,
            exit = slideOutHorizontally(targetOffsetX = { it}) + ExitTransition.None,
            modifier = Modifier.fillMaxHeight()
                .width(LocalConfiguration.current.screenWidthDp.dp * 0.75f)
                .padding(WindowInsets.statusBars.asPaddingValues())
                .navigationBarsPadding()
                .align(Alignment.CenterEnd)
                .background(Color.LightGray)
                .zIndex(1f)
        ) {
            ChatRoomSettingContent(viewModel = viewModel,
                onClose = {showSettingsPanel = false},
                onKickUser = {target ->
                    kickTarget = target
                    showKickDialog = true
                },
                onLeaveRoom = {
                    showLeaveDialog = true
                },
                explodeRoom = {
                    showExplodDialog = true
                }
            )
        }

        if (explodedState) {
            ExplosionEffect(onExploded = {
                navController.previousBackStackEntry?.savedStateHandle?.set("explodedRoomId",roomId)

                navController.popBackStack()
            })
        }

    }
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false},
            title = { Text("채팅방 나가기")},
            text = { Text("채팅방에서 나가시겠습니까?")},
            confirmButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                    viewModel.leaveRoom {
                        navController.popBackStack()
                    }
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLeaveDialog = false
                }) {
                    Text("취소")
                }
            }
        )
    }

    val currentKickTarget = kickTarget
    if (showKickDialog && currentKickTarget != null) {
        AlertDialog(
            title = { Text("추방 하기")},
            text = { Text("${currentKickTarget.name}님을 추방하시겠습니까?")},
            onDismissRequest = {
                showKickDialog = false
                kickTarget = null
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.kickUser(currentKickTarget) {
                        }
                        showKickDialog = false
                        kickTarget = null
                    }
                ) {
                    Text("추방")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showKickDialog = false
                        kickTarget = null
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }

    if (showExplodDialog) {
        AlertDialog(
            onDismissRequest = { showExplodDialog = false},
            title = { Text("채팅방 폭파!!")},
            text = { Text("채팅방을 폭파 하시겠습니까?")},
            confirmButton = {
                TextButton(onClick = {
                    showExplodDialog = false
                    showSettingsPanel = false
                    viewModel.triggerExplosion()
                }) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExplodDialog = false
                }) {
                    Text("취소")
                }
            }
        )
    }
}
