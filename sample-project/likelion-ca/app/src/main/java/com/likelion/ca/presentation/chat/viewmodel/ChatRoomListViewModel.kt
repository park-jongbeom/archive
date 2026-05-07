package com.likelion.ca.presentation.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.likelion.ca.domain.model.ChatUser
import com.likelion.ca.domain.repository.UserRepository
import com.likelion.ca.domain.repository.ChatRoomRepository
import com.likelion.ca.domain.model.ChatRoom
import com.likelion.ca.domain.usecase.GetChatRoomsUseCase
import com.likelion.ca.presentation.chat.ui.ChatRoomListState
import com.likelion.ca.presentation.chat.ui.ChatRoomTab
import com.likelion.ca.core.ui.error.UiEvent
import com.likelion.ca.domain.error.AppError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
/**
 * 채팅방 목록 화면의 상태/동작을 담당하는 ViewModel입니다.
 */
class ChatRoomListViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRoomRepository: ChatRoomRepository,
    private val getChatRoomsUseCase: GetChatRoomsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ChatRoomListState())
    val state: StateFlow<ChatRoomListState> = _state.asStateFlow()

    private val _uiEvents = MutableSharedFlow<UiEvent>(extraBufferCapacity = 1)
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            getChatRoomsUseCase().onSuccess { result ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    chatRooms = result.all,
                    joinedRooms = result.joined,
                    notJoinedRooms = result.notJoined
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(isLoading = false, error = error.message)
                _uiEvents.emit(UiEvent.ShowSnackbar(error.message))
            }
        }
    }

    fun createChatRoom(title: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val currentMe = userRepository.meOrNull ?: throw AppError.Auth("사용자 정보가 없습니다.")
                
                val room = ChatRoom(
                    title = title,
                    owner = currentMe,
                    createdAt = System.currentTimeMillis()
                )
                withContext(Dispatchers.IO) { chatRoomRepository.createChatRoom(room) }
                refresh()
            } catch (e: Exception) {
                val errorMsg = if (e is AppError) e.message else "채팅방 생성에 실패했습니다."
                _state.value = _state.value.copy(isLoading = false)
                _uiEvents.emit(UiEvent.ShowSnackbar(errorMsg))
            }
        }
    }

    fun toggleRoomLock(room: ChatRoom) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { 
                    chatRoomRepository.toggleLock(!room.isLocked, room.id) 
                }
                refresh()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowSnackbar("잠금 상태 변경 실패"))
            }
        }
    }

    fun switchTab(tab: ChatRoomTab) {
        _state.value = _state.value.copy(currentTab = tab)
    }

    fun removeChatRoom(roomId: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) { chatRoomRepository.deleteChatRoomToRemote(roomId) }
                refresh()
            } catch (e: Exception) {
                _uiEvents.emit(UiEvent.ShowSnackbar("채팅방 삭제 실패"))
            }
        }
    }
}
