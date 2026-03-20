package com.likelion.liontalk2.features.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import com.likelion.liontalk2.features.chat.data.model.ChatMessage

@Composable
/**
 * 내 메시지를 말풍선 형태로 표시합니다.
 */
fun MyMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Column(horizontalAlignment = Alignment.End) {
            val bubbleColor = Color(Color.Yellow.value)
            Box(
                modifier = Modifier.background(
                    color = bubbleColor,
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .widthIn(max = 280.dp)
            ) {
                if (message.type == "image") {
                    if (message.imageUrl != null) {
                        SubcomposeAsyncImage(
                            model = message.imageUrl,
                            contentDescription = "chat-image",
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .height(180.dp),
                            loading = { },
                            error = { },
                            success = { SubcomposeAsyncImageContent() }
                        )
                        if (message.content.isNotBlank()) {
                            Text(
                                text = message.content,
                                fontSize = 14.sp,
                                color = Color.Black,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                    } else {
                        Text(
                            text = "이미지 로딩 실패",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                } else {
                    Text(
                        text = message.content,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
