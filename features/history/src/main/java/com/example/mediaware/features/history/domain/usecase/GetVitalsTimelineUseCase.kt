package com.example.mediaware.features.history.domain.usecase

import com.example.mediaware.features.history.domain.engine.LongitudinalNarrativeEngine
import com.example.mediaware.features.history.domain.model.NarrativeTrend
import com.example.mediaware.features.history.domain.model.VitalRecord
import com.example.mediaware.features.history.domain.model.VitalType
import javax.inject.Inject

data class VitalsTimelineResult(
    val records: List<VitalRecord>,
    val trend: NarrativeTrend?
)

class GetVitalsTimelineUseCase @Inject constructor() {

    operator fun invoke(vitalType: VitalType): VitalsTimelineResult {
        val records = emptyList<VitalRecord>()
        val trend = if (records.size >= 2) {
            LongitudinalNarrativeEngine.computeTrend(records, isHigherBetter = false)
        } else {
            null
        }
        return VitalsTimelineResult(records = records, trend = trend)
    }
}
