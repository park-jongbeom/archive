package com.likelion.liontalk.features.chat.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.likelion.liontalk.core.model.ChatUser
import com.likelion.liontalk.features.chat.data.model.isSameUser
import com.likelion.liontalk.features.chat.viewmodel.ChatRoomViewModel

@Composable
/**
 * 채팅방 설정 패널(참가자 목록/잠금/나가기/추방)을 구성하는 UI 컴포저블입니다.
 */
fun ChatRoomSettingContent(
    viewModel: ChatRoomViewModel,
    onClose:() -> Unit,
    onLeaveRoom:() -> Unit,
    onKickUser:(user:ChatUser) -> Unit,
    explodeRoom:() -> Unit
) {
    val room by viewModel.room.collectAsState()
    val users = room?.users ?: emptyList()
    val me = viewModel.me

    LazyColumn(
        modifier = Modifier.fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("채팅방 설정", style = MaterialTheme.typography.titleLarge)

                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "닫기")
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("참가자 목록", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
        }
        items(users) { user ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                  modifier = Modifier.size(30.dp).clip(CircleShape)
                ) {
                    SubcomposeAsyncImage(
                        model = user.avatarUrl,
                        contentDescription = "avatar",
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "default icon",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "default icon",
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        success = { SubcomposeAsyncImageContent() }
                    )
                }

                Spacer(Modifier.width(8.dp))
                Text(user.name)
                if (room?.owner?.isSameUser(user) == true) {
                    Text(" (방장)", fontWeight = FontWeight.Bold, color = Color.Gray)
                }

                Spacer(Modifier.weight(1f))
                if (room?.owner?.isSameUser(me) == true && !me?.isSameUser(user)!!) {
                    IconButton(onClick = { onKickUser(user)}) {
                        Icon(Icons.Default.Delete, contentDescription = "추방")
                    }
                }
            }
        }

        if (room?.owner?.isSameUser(me) == true) {
            item {
                Spacer(Modifier.height(24.dp))
                Text("방 설정" , style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("잠금")
                    Spacer(Modifier.weight(1f))
                    room?.isLocked?.let {
                        Switch(checked = it, onCheckedChange = {
                            viewModel.toggleRoomLock(it)
                        })
                    }
                }

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        explodeRoom()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("방 폭파!!!")
                }
            }
        } else {
            item {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onLeaveRoom() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("채팅방 나가기")
                }
            }
        }
    }
}
