package com.likelion.liontalk.features.chat.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.likelion.liontalk.core.navigation.Screen
import com.likelion.liontalk.core.ui.error.UiEvent
import com.likelion.liontalk.features.chat.data.model.isSameUser
import com.likelion.liontalk.features.chat.viewmodel.ChatRoomListViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
/**
 * 채팅방 목록을 표시하고, 채팅방 생성/삭제/잠금 토글을 처리하는 화면입니다.
 */
fun ChatRoomListScreen(navController : NavHostController) {

    val backStackEntry = navController.currentBackStackEntry ?: return
    val viewModel: ChatRoomListViewModel = viewModel(viewModelStoreOwner = backStackEntry)

    val state by viewModel.state.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var newRoomName by remember { mutableStateOf("") }
    var showAddRoomDialog by remember { mutableStateOf(false) }

    val tabtitles = mapOf(
        ChatRoomTab.JOINED to "참여중",
        ChatRoomTab.NOT_JOINED to "미참여"
    )

    val explodedRoomId = navController.currentBackStackEntry?.savedStateHandle?.get<String>("explodedRoomId")
    LaunchedEffect(explodedRoomId) {
        explodedRoomId?.let {
            viewModel.removeChatRoom(explodedRoomId)

            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("explodedRoomId")

        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShowDialog -> Unit
            }
        }
    }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text( text = "LionTalk",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        showAddRoomDialog = true
                    }){
                        Icon(Icons.Default.Add, contentDescription = "채팅방 생성")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.SettingScreen.route)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = "설정")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { padding ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(padding)
            ) {

                TabRow(selectedTabIndex = state.currentTab.ordinal) {
                    ChatRoomTab.values().forEach { tab ->
                        Tab(
                           selected = state.currentTab == tab,
                            onClick = { viewModel.switchTab(tab)},
                            text = { Text(tabtitles[tab] ?: tab.name)}
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if ( state.isLoading ) {
                    Box(modifier = Modifier.fillMaxSize(),
                        contentAlignment =  Alignment.Center) {
                        CircularProgressIndicator()
                    }

                } else {
                    val rooms = when (state.currentTab) {
                        ChatRoomTab.JOINED -> state.joinedRooms
                        ChatRoomTab.NOT_JOINED -> state.notJoinedRooms
                    }
                    if (rooms.isEmpty()){
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) { Text("채팅방이 없습니다.") }
                    } else{
                        LazyColumn {
                            items(rooms, key = { it.id }) { room ->
                                val currentMe = viewModel.me
                                ChatRoomItem(
                                    room = room,
                                    isOwner = currentMe != null && room.owner.isSameUser(currentMe),
                                    onClick = {
                                        val isMeOwner = currentMe != null && room.owner.isSameUser(
                                            currentMe
                                        )
                                        val isMeparticipant = currentMe != null && room.users.any { it.isSameUser(
                                            currentMe
                                        ) }
                                        if (!room.isLocked || isMeOwner || isMeparticipant) {
                                            navController.navigate(
                                                Screen.ChatRoomScreen.createRoute(room.id)
                                            )
                                        } else {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("잠긴 방입니다.")
                                            }
                                        }
                                    },
                                    onLongPressDelete = { target ->
                                        if (currentMe != null && target.owner.isSameUser(currentMe)) {
                                            viewModel.removeChatRoom(target.id)
                                        }
                                    },
                                    onLongPressLock = { target ->
                                        if (currentMe != null && target.owner.isSameUser(currentMe)) {
                                            viewModel.toggleRoomLock(target)
                                        }
                                    })
                            }
                        }
                    }
                }
            }
        }
    )
    if (showAddRoomDialog) {
        AlertDialog(
            onDismissRequest = { showAddRoomDialog = false },
            title = { Text("채팅방 생성")},
            text = {
                OutlinedTextField(
                    value = newRoomName,
                    onValueChange = { newRoomName = it},
                    label = { Text("채팅방 제목")},
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newRoomName.isNotBlank()) {
                        viewModel.createChatRoom(newRoomName)
                        newRoomName = ""
                        showAddRoomDialog = false
                    }
                }) {
                    Text("생성")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddRoomDialog = false
                }) {
                    Text("취소")
                }
            }
        )
    }
}
