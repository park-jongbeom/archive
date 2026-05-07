package com.likelion.liontalk.core.navigation

/**
 * Compose Navigation에서 사용하는 라우트 정의를 담고 있는 시드 클래스입니다.
 */
sealed class Screen(val route: String) {
    object ChatRoomListScreen : Screen("chatroom_list")
    object ChatRoomScreen : Screen("chatroom_detail/{roomId}") {
        /**
         * `roomId`를 포함한 채팅방 상세 라우트 문자열을 생성합니다.
         */
        fun createRoute(roomId : String) = "chatroom_detail/$roomId"
    }
    object SettingScreen : Screen("setting")
    object LauncherScreen : Screen("launcher")

    object SignScreen : Screen("sign")
}
