package com.likelion.liontalk.core.di

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateRegistryOwner
import com.likelion.liontalk.features.auth.viewmodel.SignViewModel
import com.likelion.liontalk.features.chat.viewmodel.ChatRoomListViewModel
import com.likelion.liontalk.features.chat.viewmodel.ChatRoomViewModel
import com.likelion.liontalk.features.launcher.viewmodel.LauncherViewModel
import com.likelion.liontalk.features.setting.viewmodel.SettingViewModel

/**
 * 수동 DI 컨테이너(AppContainer)를 기반으로 ViewModel을 생성하는 Factory입니다.
 *
 * Navigation의 SavedStateHandle이 필요한 화면(ChatRoom 상세)에서도 동일하게 동작하도록
 * `AbstractSavedStateViewModelFactory`를 사용합니다.
 */
class ViewModelFactory(
    private val appContainer: AppContainer,
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle? = null,
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

    override fun <T : ViewModel> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle,
    ): T {
        @Suppress("UNCHECKED_CAST")
        return when {
            modelClass.isAssignableFrom(LauncherViewModel::class.java) ->
                LauncherViewModel(appContainer.userRepository) as T

            modelClass.isAssignableFrom(SignViewModel::class.java) ->
                SignViewModel(
                    authRepository = appContainer.authRepository,
                    userRepository = appContainer.userRepository,
                ) as T

            modelClass.isAssignableFrom(SettingViewModel::class.java) ->
                SettingViewModel(
                    userRepository = appContainer.userRepository,
                ) as T

            modelClass.isAssignableFrom(ChatRoomListViewModel::class.java) ->
                ChatRoomListViewModel(
                    userRepository = appContainer.userRepository,
                    chatRoomRepository = appContainer.chatRoomRepository,
                ) as T

            modelClass.isAssignableFrom(ChatRoomViewModel::class.java) ->
                ChatRoomViewModel(
                    userRepository = appContainer.userRepository,
                    chatMessageRepository = appContainer.chatMessageRepository,
                    chatRoomRepository = appContainer.chatRoomRepository,
                    eventRepository = appContainer.chatRoomEventRepository,
                    storageRepository = appContainer.storageRepository,
                    savedStateHandle = handle,
                ) as T

            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
