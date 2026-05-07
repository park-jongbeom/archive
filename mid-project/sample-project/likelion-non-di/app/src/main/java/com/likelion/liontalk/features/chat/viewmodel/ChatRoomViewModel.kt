package com.likelion.liontalk.features.chat.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModel
import com.likelion.liontalk.core.data.model.ChatUser
import com.likelion.liontalk.core.data.repository.StorageRepository
import com.likelion.liontalk.core.data.repository.UserRepository
import com.likelion.liontalk.core.ui.error.UiEvent
import com.likelion.liontalk.features.chat.data.repository.ChatMessageRepository
import com.likelion.liontalk.features.chat.data.repository.ChatRoomRepository
import com.likelion.liontalk.features.chat.data.repository.ChatRoomEventRepository
import com.likelion.liontalk.features.chat.data.model.ChatMessage
import com.likelion.liontalk.features.chat.data.model.ChatRoom
import com.likelion.liontalk.features.chat.data.model.RoomEvent
import com.likelion.liontalk.features.chat.data.model.isSameUser
import com.likelion.liontalk.features.chat.ui.ChatRoomEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * 채팅방 상세 화면에서 메시지/이벤트 상태를 관리하고, 전송/입장/퇴장 등의 동작을 수행하는 ViewModel입니다.
 */
class ChatRoomViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private companion object {
        private const val TAG = "ChatRoomViewModel"
    }

    private val userRepository = UserRepository
    private val chatMessageRepository = ChatMessageRepository
    private val chatRoomRepository = ChatRoomRepository
    private val eventRepository = ChatRoomEventRepository
    private val storageRepository = StorageRepository

    private val roomId: String = savedStateHandle.get<String>("roomId")
        ?: error("roomId is missing (expected navigation argument 'roomId')")

    private var eventListenerJob: Job? = null
    private val processedEventIds = mutableSetOf<String>()

    private val _systemMessages = MutableStateFlow<List<ChatMessage>>(emptyList())

    /** 채팅방의 메시지 목록(실시간 스트림)입니다. */
    val messages: StateFlow<List<ChatMessage>> = combine(
        chatMessageRepository.getMessagesForRoomFlow(roomId),
        _systemMessages
    ) { dbMessages, systemMessages ->
        (dbMessages + systemMessages).sortedBy { it.createdAt }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /** 현재 로그인된 사용자 정보입니다. */
    val me: ChatUser? get() = userRepository.meOrNull

    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    /** 화면에서 구독하는 일회성 UI 이벤트 스트림입니다. */
    val uiEvents = _uiEvents.asSharedFlow()

    private fun Throwable.toUserMessage(default: String): String {
        val msg = message?.trim()
        return msg?.takeIf { it.isNotBlank() } ?: default
    }

    private val _room = MutableStateFlow<ChatRoom?>(null)
    /** 현재 채팅방 정보입니다. */
    val room: StateFlow<ChatRoom?> = _room

    private val _event = MutableSharedFlow<ChatRoomEvent>()
    /** 채팅 화면에서 처리할 내부 이벤트 스트림입니다. */
    val event = _event.asSharedFlow()

    private val _explodeState = MutableStateFlow(false)
    /** 폭파 UI 상태 여부입니다. */
    val explodeState: StateFlow<Boolean> = _explodeState

    private var typing = false
    private var typingStopJop: Job? = null

    init {
        // 메시지 스트림(=FireStore)에 변화가 생기면 스크롤 이벤트를 발생시킨다.
        viewModelScope.launch {
            var lastSize = 0
            messages.collect { list ->
                if (list.size > lastSize) {
                    lastSize = list.size
                    _event.emit(ChatRoomEvent.ScrollToBottom)
                }
            }
        }

        viewModelScope.launch {
            try {
                if (userRepository.meOrNull == null) {
                    _uiEvents.emit(UiEvent.ShowSnackbar("사용자 정보를 불러오지 못했어요. 다시 로그인해주세요."))
                    return@launch
                }
                startFirestoreEventListener()
                loadRoomInfo()
                publishEnterStatus()
            } catch (e: Exception) {
                Log.e(TAG, "init error", e)
                _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("채팅방을 불러오지 못했어요.")))
            }
        }
    }

    private fun startFirestoreEventListener() {
        eventListenerJob?.cancel()
        eventListenerJob = viewModelScope.launch(Dispatchers.IO) {
            eventRepository.listenEvents(roomId)
                .catch { e ->
                    Log.e(TAG, "listenEvents error", e)
                    _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("채팅 이벤트 수신에 실패했어요.")))
                }
                .collect { list ->
                    list.forEach { evt ->
                        if (evt.id !in processedEventIds) {
                            processedEventIds.add(evt.id)
                            handleRoomEvent(evt)
                        }
                    }
                }
        }
    }

    private fun handleRoomEvent(evt: RoomEvent) {
        val currentMe = me ?: return
        val senderUser = evt.senderUser ?: ChatUser(
            id = "",
            name = evt.sender.orEmpty(),
            avatarUrl = null
        )
        val targetUser = evt.targetUser ?: ChatUser(
            id = "",
            name = evt.target.orEmpty(),
            avatarUrl = null
        )

        when (evt.type) {
            "enter" -> if (!senderUser.isSameUser(currentMe)) {
                viewModelScope.launch {
                    _event.emit(ChatRoomEvent.ChatRoomEnter(senderUser))
                    postSystemMessage("${senderUser.name} 님이 입장하였습니다.")
                }
            }

            "leave" -> if (!senderUser.isSameUser(currentMe)) {
                viewModelScope.launch {
                    _event.emit(ChatRoomEvent.ChatRoomLeave(senderUser))
                    postSystemMessage("${senderUser.name} 님이 퇴장하였습니다.")
                }
            }

            "typing" -> if (!senderUser.isSameUser(currentMe)) {
                viewModelScope.launch {
                    val e = if (evt.typing == true) ChatRoomEvent.TypingStarted(senderUser)
                    else ChatRoomEvent.TypingStopped
                    _event.emit(e)
                }
            }

            "kick" -> if (targetUser.isSameUser(currentMe)) {
                viewModelScope.launch { _event.emit(ChatRoomEvent.Kicked) }
            }

            "explod" -> if (!senderUser.isSameUser(currentMe)) {
                _explodeState.value = true
            }

            "lock" -> if (!senderUser.isSameUser(currentMe)) {
                // Firestore only이므로 로컬 동기화 대신 원격에서 room을 다시 가져온다.
                viewModelScope.launch {
                    _room.value = chatRoomRepository.getRoomFromRemote(roomId)
                }
            }
        }
    }

    private fun loadRoomInfo() {
        viewModelScope.launch {
            chatRoomRepository
                .getChatRoomFlow(roomId)
                .catch { e ->
                    Log.e(TAG, "loadRoomInfo(roomId=$roomId) failed", e)
                    _room.value = null
                    _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("채팅방을 불러오지 못했어요.")))
                }
                .collect { room ->
                    _room.value = room
                }
        }
    }

    /**
     * 텍스트 메시지를 채팅방에 전송합니다.
     */
    fun sendMessage(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sender = me ?: run {
                _uiEvents.emit(UiEvent.ShowSnackbar("사용자 정보를 불러오지 못했어요."))
                return@launch
            }
            try {
                val message = ChatMessage(
                    roomId = roomId,
                    sender = sender,
                    content = content,
                    createdAt = System.currentTimeMillis()
                )
                chatMessageRepository.sendMessage(message)
                publishTypingStatus(false)
                _event.emit(ChatRoomEvent.ClearInput)
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage failed", e)
                _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("메시지 전송에 실패했어요.")))
            }
        }
    }

    /**
     * 이미지를 업로드한 뒤 캡션을 포함해 채팅방에 전송합니다.
     */
    fun sendImage(imageUri: android.net.Uri, caption: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val sender = me ?: run {
                _uiEvents.emit(UiEvent.ShowSnackbar("사용자 정보를 불러오지 못했어요."))
                return@launch
            }
            try {
                val imageUrl = storageRepository.uploadChatImage(roomId, sender.name, imageUri)
                val message = ChatMessage(
                    roomId = roomId,
                    sender = sender,
                    content = caption,
                    type = "image",
                    imageUrl = imageUrl,
                    createdAt = System.currentTimeMillis()
                )
                chatMessageRepository.sendMessage(message)
                publishTypingStatus(false)
                _event.emit(ChatRoomEvent.ClearInput)
            } catch (e: Exception) {
                Log.e(TAG, "sendImage failed", e)
                _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("이미지 전송에 실패했어요.")))
            }
        }
    }

    private fun publishTypingStatus(isTyping: Boolean) {
        val sender = me ?: return
        viewModelScope.launch(Dispatchers.IO) {
            eventRepository.sendEvent(roomId, "typing", sender = sender, typing = isTyping)
        }
    }

    /**
     * 채팅방의 잠금 상태를 원격 이벤트로 변경합니다.
     */
    fun toggleRoomLock(isLock: Boolean) {
        viewModelScope.launch {
            chatRoomRepository.toggleLock(isLock, roomId)
            publishLockStatus()
        }
    }

    private fun publishLockStatus() {
        val sender = me ?: return
        viewModelScope.launch(Dispatchers.IO) {
            eventRepository.sendEvent(roomId, "lock", sender = sender)
        }
    }

    /**
     * 폭파 이벤트를 발행합니다.
     */
    fun triggerExplosion() {
        _explodeState.value = true
        viewModelScope.launch { publishExplod() }
    }

    private fun publishExplod() {
        val sender = me ?: return
        viewModelScope.launch(Dispatchers.IO) {
            eventRepository.sendEvent(roomId, "explod", sender = sender)
        }
    }

    private fun postSystemMessage(content: String) {
        val sender = me ?: return
        val systemMessage = ChatMessage(
            id = "system-${System.currentTimeMillis()}",
            roomId = roomId,
            sender = sender,
            content = content,
            type = "system",
            createdAt = System.currentTimeMillis()
        )
        _systemMessages.value = _systemMessages.value + systemMessage
    }

    /**
     * 입력 값 변경을 기반으로 "typing" 상태를 갱신합니다.
     */
    fun onTypingChanged(text: String) {
        if (text.isNotBlank() && !typing) {
            typing = true
            publishTypingStatus(true)
        }
        typingStopJop?.cancel()
        typingStopJop = viewModelScope.launch {
            delay(2000)
            typing = false
            publishTypingStatus(false)
        }
    }

    /**
     * typing 상태를 즉시 중단합니다.
     */
    fun stopTyping() {
        typing = false
        publishTypingStatus(false)
        typingStopJop?.cancel()
    }

    private fun publishEnterStatus() {
        val sender = me ?: return
        viewModelScope.launch(Dispatchers.IO) {
            eventRepository.sendEvent(roomId, "enter", sender = sender)
        }
        viewModelScope.launch(Dispatchers.IO) {
            _room.value = chatRoomRepository.enterRoom(sender, roomId)
        }
    }

    private fun publishLeaveStatus() {
        val sender = me ?: return
        viewModelScope.launch(Dispatchers.IO) {
            eventRepository.sendEvent(roomId, "leave", sender = sender)
        }
    }

    /**
     * 채팅방에서 나가기 처리 후, 완료 콜백을 호출합니다.
     */
    fun leaveRoom(onComplete: () -> Unit) {
        viewModelScope.launch {
            val currentMe = me
            if (currentMe != null) {
                chatRoomRepository.removeUserFromRoom(currentMe, roomId)
                publishLeaveStatus()
            }
            launch(Dispatchers.Main) { onComplete() }
        }
    }

    /**
     * 특정 사용자를 방에서 퇴장(추방)시키고 완료 콜백을 호출합니다.
     */
    fun kickUser(user: ChatUser, onComplete: () -> Unit) {
        viewModelScope.launch {
            val sender = me ?: run {
                _uiEvents.emit(UiEvent.ShowSnackbar("사용자 정보를 불러오지 못했어요."))
                return@launch
            }
            try {
                chatRoomRepository.removeUserFromRoom(user, roomId)
                withContext(Dispatchers.IO) {
                    eventRepository.sendEvent(roomId, "kick", sender = sender, target = user)
                }
                launch(Dispatchers.Main) { onComplete() }
            } catch (e: Exception) {
                Log.e(TAG, "kickUser failed", e)
                _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("추방에 실패했어요.")))
            }
        }
    }

    /**
     * 채팅방을 떠날 때 내부 리스너를 정리하고 완료 콜백을 호출합니다.
     */
    fun back(onComplete: () -> Unit) {
        viewModelScope.launch {
            eventListenerJob?.cancel()
            onComplete()
        }
    }
}

