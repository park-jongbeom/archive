package com.likelion.ca.presentation.chat.viewmodel

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.likelion.ca.core.ui.error.UiEvent
import com.likelion.ca.domain.model.ChatMessage
import com.likelion.ca.domain.model.ChatUser
import com.likelion.ca.domain.model.RoomEvent
import com.likelion.ca.domain.model.isSameUser
import com.likelion.ca.domain.repository.ChatRoomRepository
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.usecase.*
import com.likelion.ca.presentation.chat.ui.ChatRoomEvent
import com.likelion.ca.presentation.chat.ui.ChatRoomUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val listenRoomEventsUseCase: ListenRoomEventsUseCase,
    private val sendRoomEventUseCase: SendRoomEventUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val uploadChatImageUseCase: UploadChatImageUseCase,
    private val leaveRoomUseCase: LeaveRoomUseCase,
    private val kickUserUseCase: KickUserUseCase,
    private val enterChatRoomUseCase: EnterChatRoomUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: String = savedStateHandle.get<String>("roomId")
        ?: error("roomId is missing")

    private val _uiState = MutableStateFlow(ChatRoomUiState())
    val uiState: StateFlow<ChatRoomUiState> = _uiState.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvents = _uiEvents.asSharedFlow()

    private val _chatEvents = MutableSharedFlow<ChatRoomEvent>()
    val chatEvents = _chatEvents.asSharedFlow()

    private val _systemMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val processedEventIds = mutableSetOf<String>()
    
    private var typingJob: Job? = null
    private var eventListenerJob: Job? = null

    val me: ChatUser? get() = userRepository.meOrNull

    init {
        initRoom()
        observeMessages()
        observeEvents()
    }

    private fun initRoom() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            enterChatRoomUseCase(roomId).onSuccess { room ->
                _uiState.update { it.copy(isLoading = false, room = room) }
                sendRoomEventUseCase(roomId, "enter")
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
                _uiEvents.emit(UiEvent.ShowSnackbar(error.message))
            }
        }
    }

    private fun observeMessages() {
        combine(
            getMessagesUseCase(roomId),
            _systemMessages
        ) { dbMessages, systemMessages ->
            (dbMessages + systemMessages).sortedBy { it.createdAt }
        }.onEach { messages ->
            _uiState.update { it.copy(messages = messages) }
            _chatEvents.emit(ChatRoomEvent.ScrollToBottom)
        }.launchIn(viewModelScope)
    }

    private fun observeEvents() {
        eventListenerJob = listenRoomEventsUseCase(roomId)
            .onEach { events ->
                events.forEach { event ->
                    if (event.id !in processedEventIds) {
                        processedEventIds.add(event.id)
                        handleRoomEvent(event)
                    }
                }
            }.launchIn(viewModelScope)
    }

    private fun handleRoomEvent(event: RoomEvent) {
        val currentMe = me ?: return
        val sender = event.senderUser ?: return
        
        if (sender.isSameUser(currentMe)) return

        viewModelScope.launch {
            when (event.type) {
                "enter" -> {
                    _chatEvents.emit(ChatRoomEvent.ChatRoomEnter(sender))
                    postSystemMessage("${sender.name} 님이 입장하였습니다.")
                }
                "leave" -> {
                    _chatEvents.emit(ChatRoomEvent.ChatRoomLeave(sender))
                    postSystemMessage("${sender.name} 님이 퇴장하였습니다.")
                }
                "typing" -> {
                    val chatEvent = if (event.typing == true) ChatRoomEvent.TypingStarted(sender) 
                                   else ChatRoomEvent.TypingStopped
                    _chatEvents.emit(chatEvent)
                }
                "kick" -> {
                    if (event.targetUser?.isSameUser(currentMe) == true) {
                        _chatEvents.emit(ChatRoomEvent.Kicked)
                    }
                }
                "explod" -> {
                    _uiState.update { it.copy(explodeState = true) }
                }
                "lock" -> {
                    // Refresh room info
                    val updatedRoom = chatRoomRepository.getRoomFromRemote(roomId)
                    _uiState.update { it.copy(room = updatedRoom) }
                }
            }
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            sendMessageUseCase(roomId, content).onSuccess {
                sendRoomEventUseCase(roomId, "typing", typing = false)
                _chatEvents.emit(ChatRoomEvent.ClearInput)
            }.onFailure { error ->
                _uiEvents.emit(UiEvent.ShowSnackbar(error.message))
            }
        }
    }

    fun sendImage(localUri: String, caption: String) {
        viewModelScope.launch {
            uploadChatImageUseCase(roomId, localUri).onSuccess { imageUrl ->
                sendMessageUseCase(roomId, caption, imageUrl).onSuccess {
                    _chatEvents.emit(ChatRoomEvent.ClearInput)
                }
            }.onFailure { error ->
                _uiEvents.emit(UiEvent.ShowSnackbar(error.message))
            }
        }
    }

    fun onTypingChanged(text: String) {
        if (text.isNotBlank()) {
            sendRoomEventUseCase(roomId, "typing", typing = true)
        }
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            delay(2000)
            sendRoomEventUseCase(roomId, "typing", typing = false)
        }
    }

    fun leaveRoom(onComplete: () -> Unit) {
        viewModelScope.launch {
            leaveRoomUseCase(roomId).onSuccess {
                onComplete()
            }
        }
    }

    fun kickUser(user: ChatUser, onComplete: () -> Unit) {
        viewModelScope.launch {
            kickUserUseCase(roomId, user).onSuccess {
                onComplete()
            }.onFailure { error ->
                _uiEvents.emit(UiEvent.ShowSnackbar(error.message))
            }
        }
    }

    fun triggerExplosion() {
        _uiState.update { it.copy(explodeState = true) }
        viewModelScope.launch {
            sendRoomEventUseCase(roomId, "explod")
        }
    }

    private fun postSystemMessage(content: String) {
        val systemMessage = ChatMessage(
            id = "system-${System.currentTimeMillis()}",
            roomId = roomId,
            sender = ChatUser(name = "System"),
            message = content,
            createdAt = System.currentTimeMillis()
        )
        _systemMessages.update { it + systemMessage }
    }

    override fun onCleared() {
        super.onCleared()
        eventListenerJob?.cancel()
    }
}
