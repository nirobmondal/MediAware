package com.example.mediaware.features.auth.domain.usecase

import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.core.common.security.PinSecurityManager
import com.example.mediaware.features.auth.domain.repository.UserRepository
import javax.inject.Inject

class VerifyPinUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(enteredPin: String): Resource<Boolean> {
        val credentials = userRepository.getUserCredentials()
            ?: return Resource.Error("কোনো ব্যবহারকারী পাওয়া যায়নি")

        val isValid = PinSecurityManager.verifyPin(
            enteredPin = enteredPin,
            storedHash = credentials.pinHash,
            storedSalt = credentials.pinSalt
        )

        return if (isValid) {
            Resource.Success(true)
        } else {
            Resource.Error("ভুল পিন দেওয়া হয়েছে")
        }
    }
}
