package com.likelion.ca.presentation.chat.ui.components

import androidx.compose.runtime.Composable
import com.likelion.ca.domain.model.ChatMessage

@Composable
/**
 * 메시지 타입에 따라 적절한 UI 컴포넌트를 그리는 메시지 렌더러입니다.
 */
fun ChatMessageItem(message: ChatMessage, isMe : Boolean) {
    when {
        message.type == "system" -> SystemMessageItem(message.content)
        isMe -> MyMessageItem(message)
        else -> OtherMessageItem(message)
    }
}
