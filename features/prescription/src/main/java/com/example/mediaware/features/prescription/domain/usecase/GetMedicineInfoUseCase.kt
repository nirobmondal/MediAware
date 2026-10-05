package com.example.mediaware.features.prescription.domain.usecase

import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.features.prescription.domain.model.MedicineExplanation
import com.example.mediaware.features.prescription.domain.repository.MedicineRepository
import javax.inject.Inject

/**
 * UseCase coordinating Smart Cache retrieval for drug explanations.
 * Strictly enforces:
 * - Guardrail #2: NEVER alter prescribed dosages.
 * - Guardrail #3: Mandatory clinical deferral disclaimer.
 */
class GetMedicineInfoUseCase @Inject constructor(
    private val repository: MedicineRepository
) {
    suspend operator fun invoke(drugName: String): Resource<MedicineExplanation> {
        val result = repository.getMedicineExplanation(drugName)
        return when (result) {
            is Resource.Success -> {
                val data = result.data
                if (data != null) {
                    Resource.Success(
                        data = data.copy(
                            mandatoryDisclaimerBn = "এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।"
                        ),
                        isFromCache = result.isFromCache
                    )
                } else {
                    Resource.Error("ওষুধের বিবরণ পাওয়া যায়নি।")
                }
            }
            is Resource.Error -> Resource.Error(result.messageBn)
            is Resource.Loading -> Resource.Loading
        }
    }
}
