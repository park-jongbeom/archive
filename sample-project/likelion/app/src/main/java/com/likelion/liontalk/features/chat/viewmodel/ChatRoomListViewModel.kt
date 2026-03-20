package com.likelion.liontalk2.features.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.likelion.liontalk2.core.data.auth.AuthStateRepository
import com.likelion.liontalk2.core.model.ChatUser
import com.likelion.liontalk2.features.chat.data.repository.ChatRoomRepository
import com.likelion.liontalk2.features.chat.data.model.ChatRoom
import com.likelion.liontalk2.features.chat.data.model.isSameUser
import com.likelion.liontalk2.features.chat.ui.ChatRoomListState
import com.likelion.liontalk2.features.chat.ui.ChatRoomTab
import com.likelion.liontalk2.core.ui.error.UiEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 채팅방 목록 화면의 상태/동작을 담당하는 ViewModel입니다.
 */
class ChatRoomListViewModel @Inject constructor(
    private val authStateRepository: AuthStateRepository,
    private val chatRoomRepository: ChatRoomRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatRoomListState())
    /** 채팅방 목록 화면의 현재 상태입니다. */
    val state: StateFlow<ChatRoomListState> = _state.asStateFlow()

    /** 현재 로그인된 사용자를 반환합니다. */
    val me: ChatUser? get() = authStateRepository.meOrNull

    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    /** 화면에서 구독하는 일회성 UI 이벤트 스트림입니다. */
    val uiEvents = _uiEvents.asSharedFlow()

    private fun Throwable.toUserMessage(default: String): String {
        val msg = message?.trim()
        return msg?.takeIf { it.isNotBlank() } ?: default
    }

    init {
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val currentMe = me ?: run {
                    _state.value = _state.value.copy(isLoading = false)
                    _uiEvents.emit(UiEvent.ShowSnackbar("사용자 정보를 불러오지 못했어요."))
                    return@launch
                }
                val rooms = withContext(Dispatchers.IO) { chatRoomRepository.fetchRoomsOnce() }

                val joined = rooms.filter { it.users.any { p -> p.isSameUser(currentMe) } }
                val notJoined = rooms.filter { it.users.none { p -> p.isSameUser(currentMe) } }

                _state.value = _state.value.copy(
                    isLoading = false,
                    chatRooms = rooms,
                    joinedRooms = joined,
                    notJoinedRooms = notJoined
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
                _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("채팅방 목록 로드에 실패했습니다.")))
            }
        }
    }

    /**
     * 새로운 채팅방을 생성합니다.
     */
    fun createChatRoom(title: String) {
        viewModelScope.launch {
            try {
                val currentMe = me ?: run {
                    _uiEvents.emit(UiEvent.ShowSnackbar("사용자 정보를 불러오지 못했어요."))
                    return@launch
                }
                val room = ChatRoom(
                    id = "",
                    title = title,
                    owner = currentMe,
                    users = emptyList(),
                    isLocked = false,
                    createdAt = System.currentTimeMillis(),
                    unReadCount = 0,
                    lastReadMessageId = "",
                    lastReadMessageTimestamp = 0L
                )
                withContext(Dispatchers.IO) { chatRoomRepository.createChatRoom(room) }
                refresh()
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false, error = e.message)
                _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("채팅방 생성에 실패했습니다.")))
            }
        }
    }

    /**
     * 특정 채팅방의 잠금 상태를 토글합니다.
     */
    fun toggleRoomLock(room: ChatRoom) {
        viewModelScope.launch {
            try {
                val newLockState = !room.isLocked
                withContext(Dispatchers.IO) { chatRoomRepository.toggleLock(newLockState, room.id) }
                refresh()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("잠금 상태 변경에 실패했습니다.")))
            }
        }
    }

    /**
     * 탭 상태(참여중/미참여)를 변경합니다.
     */
    fun switchTab(tab: ChatRoomTab) {
        _state.value = _state.value.copy(currentTab = tab)
    }

    /**
     * 원격에서 채팅방을 삭제합니다.
     */
    fun removeChatRoom(roomId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { chatRoomRepository.deleteChatRoomToRemote(roomId) }
                refresh()
            } catch (e: Exception) {
                _state.value = _state.value.copy(error = e.message)
                _uiEvents.emit(UiEvent.ShowSnackbar(e.toUserMessage("채팅방 삭제에 실패했습니다.")))
            }
        }
    }
}

