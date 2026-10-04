package com.example.mediaware.features.history.domain.engine

import com.example.mediaware.features.history.domain.model.NarrativeTrend
import com.example.mediaware.features.history.domain.model.VitalRecord

/**
 * Pure Kotlin mathematical trend delta engine.
 * Computes month-over-month differences and transforms raw time-series vitals into
 * conversational Bengali story narratives tailored for patients with low mathematical literacy.
 */
object LongitudinalNarrativeEngine {

    fun computeTrend(history: List<VitalRecord>, isHigherBetter: Boolean = false): NarrativeTrend? {
        if (history.size < 2) return null
        val sorted = history.sortedBy { it.timestamp }
        val prev = sorted[sorted.size - 2]
        val curr = sorted.last()

        val diff = curr.value - prev.value
        val months = ((curr.timestamp - prev.timestamp) / (30L * 24 * 60 * 60 * 1000)).toInt().coerceAtLeast(1)

        val directionVerbBn = if (diff > 0) "বেড়ে" else if (diff < 0) "কমে" else "অপরিবর্তিত থেকে"
        val isImproving = if (isHigherBetter) diff > 0 else diff < 0
        val trendDirectionBn = if (diff > 0) "উর্ধ্বমুখী" else if (diff < 0) "নিম্নমুখী" else "স্থিতিশীল"

        val prevBn = formatNumberBn(prev.value)
        val currBn = formatNumberBn(curr.value)
        val monthsBn = formatNumberBn(months.toDouble())

        val narrative = if (diff != 0.0) {
            "গত $monthsBn মাসে আপনার ${curr.vitalType.labelBn} $prevBn থেকে $directionVerbBn $currBn ${curr.vitalType.unit} হয়েছে ($trendDirectionBn)।"
        } else {
            "গত $monthsBn মাসে আপনার ${curr.vitalType.labelBn} $currBn ${curr.vitalType.unit} এ স্থিতিশীল রয়েছে।"
        }

        return NarrativeTrend(
            testNameBn = curr.vitalType.labelBn,
            previousValue = prev.value,
            currentValue = curr.value,
            monthsElapsed = months,
            narrativeBn = narrative,
            isImproving = isImproving,
            directionBn = trendDirectionBn
        )
    }

    private fun formatNumberBn(value: Double): String {
        val intVal = value.toInt()
        val str = if (value == intVal.toDouble()) intVal.toString() else String.format("%.1f", value)
        val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        return str.map { ch ->
            if (ch in '0'..'9') bengaliDigits[ch - '0'] else ch
        }.joinToString("")
    }
}
