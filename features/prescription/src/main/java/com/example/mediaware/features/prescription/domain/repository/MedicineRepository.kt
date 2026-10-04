package com.example.mediaware.features.prescription.domain.repository

import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.features.prescription.domain.model.MedicineExplanation

interface MedicineRepository {
    suspend fun getMedicineExplanation(genericOrBrand: String): Resource<MedicineExplanation>
    suspend fun searchMedicines(query: String): List<String>
    suspend fun saveMedicineExplanation(explanation: MedicineExplanation)
}
