package com.likelion.ca.core.navigation

import com.likelion.ca.domain.usecase.AppStartDestination

fun AppStartDestination.toRoute(): String = when (this) {
    AppStartDestination.Sign -> Screen.SignScreen.route
    AppStartDestination.ProfileSetup -> Screen.SettingScreen.route
    AppStartDestination.ChatList -> Screen.ChatRoomListScreen.route
}
