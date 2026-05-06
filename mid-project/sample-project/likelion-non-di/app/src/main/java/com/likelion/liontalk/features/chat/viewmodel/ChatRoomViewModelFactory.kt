package com.likelion.liontalk.features.chat.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateRegistryOwner
import androidx.lifecycle.ViewModel

/**
 * Navigation [SavedStateHandle](roomId 등)만 전달하기 위한 Factory입니다. Repository는 싱글톤을 직접 참조합니다.
 */
class ChatRoomViewModelFactory(
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
            modelClass.isAssignableFrom(ChatRoomViewModel::class.java) ->
                ChatRoomViewModel(handle) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
