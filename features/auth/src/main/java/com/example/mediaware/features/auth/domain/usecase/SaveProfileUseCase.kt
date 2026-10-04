package com.example.mediaware.features.auth.domain.usecase

import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.core.model.Gender
import com.example.mediaware.core.domain.repository.UserRepository
import javax.inject.Inject

class SaveProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(
        name: String,
        ageString: String,
        gender: Gender,
        bloodGroup: String?,
        conditions: Set<String>
    ): Resource<Unit> {
        val cleanName = name.trim()
        if (cleanName.length < 2) {
            return Resource.Error("আপনার পূর্ণ নাম সঠিকভাবে লিখুন")
        }
        val age = ageString.toIntOrNull()
        if (age == null || age !in 1..125) {
            return Resource.Error("১ থেকে ১২৫ এর মধ্যে সঠিক বয়স দিন")
        }

        return try {
            userRepository.updateUserProfile(
                fullName = cleanName,
                age = age,
                gender = gender.name,
                bloodGroup = bloodGroup,
                chronicConditions = conditions.toList()
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error("প্রোফাইল সংরক্ষণ ব্যর্থ হয়েছে: ${e.localizedMessage ?: "অজ্ঞাত ত্রুটি"}")
        }
    }
}
