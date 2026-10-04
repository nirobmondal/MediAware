package com.example.mediaware.features.auth.domain.usecase

import com.example.mediaware.core.model.SessionStatus
import com.example.mediaware.core.domain.repository.UserRepository
import javax.inject.Inject

class CheckSessionUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): SessionStatus {
        return userRepository.checkSessionStatus()
    }
}
