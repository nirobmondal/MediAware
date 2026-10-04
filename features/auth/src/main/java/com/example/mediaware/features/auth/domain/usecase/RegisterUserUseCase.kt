package com.example.mediaware.features.auth.domain.usecase

import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.core.common.security.PinSecurityManager
import com.example.mediaware.features.auth.domain.repository.UserRepository
import java.util.UUID
import javax.inject.Inject

class RegisterUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    private val bdPhoneRegex = Regex("""^01[3-9]\d{8}$""")

    suspend operator fun invoke(phone: String, pin: String, confirmPin: String): Resource<Unit> {
        val cleanPhone = phone.trim()
        if (!bdPhoneRegex.matches(cleanPhone)) {
            return Resource.Error("সঠিক ১১ ডিজিটের বাংলাদেশী মোবাইল নম্বর দিন (যেমন: 01712345678)")
        }
        if (pin.length != 4 || !pin.all { it.isDigit() }) {
            return Resource.Error("পিন অবশ্যই ৪ সংখ্যার হতে হবে")
        }
        if (pin != confirmPin) {
            return Resource.Error("দুই পিন মেলেনি, আবার লিখুন")
        }

        return try {
            val salt = PinSecurityManager.generateSalt()
            val hash = PinSecurityManager.hashPin(pin, salt)
            val userId = UUID.randomUUID().toString()

            userRepository.registerUser(
                userId = userId,
                phone = cleanPhone,
                pinHash = hash,
                pinSalt = salt
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("নিবন্ধন ব্যর্থ হয়েছে: ${e.localizedMessage ?: "অজ্ঞাত ত্রুটি"}")
        }
    }
}
