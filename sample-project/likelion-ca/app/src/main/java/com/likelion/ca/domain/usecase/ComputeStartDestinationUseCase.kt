package com.likelion.ca.domain.usecase

import com.likelion.ca.domain.repository.UserRepository

/**
 * 로그인/프로필 상태에 따른 앱 시작 목적지를 계산합니다. 라우트 문자열은 presentation에서 매핑합니다.
 */
enum class AppStartDestination {
    Sign,
    ProfileSetup,
    ChatList,
}

class ComputeStartDestinationUseCase(
    private val userRepository: UserRepository,
) {
    suspend operator fun invoke(provider: String? = null): AppStartDestination {
        if (userRepository.authSession.value == null) return AppStartDestination.Sign
        userRepository.ensureUserProfile(provider = provider)
        val user = userRepository.meOrNull
        return if (user == null || user.name.isBlank()) {
            AppStartDestination.ProfileSetup
        } else {
            AppStartDestination.ChatList
        }
    }
}
