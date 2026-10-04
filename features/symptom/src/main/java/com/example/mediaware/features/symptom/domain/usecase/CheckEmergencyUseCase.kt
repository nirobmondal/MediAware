package com.example.mediaware.features.symptom.domain.usecase

import com.example.mediaware.features.symptom.domain.model.EmergencyTriageResult
import com.example.mediaware.features.symptom.domain.model.Urgency
import javax.inject.Inject

class CheckEmergencyUseCase @Inject constructor() {

    private data class Rule(
        val requiredIds: Set<String>,
        val requiredKeywordsBn: List<Set<String>>,
        val reasonBn: String,
        val urgency: Urgency
    )

    private val rules = listOf(
        Rule(
            requiredIds = setOf("chest_pain", "breathlessness"),
            requiredKeywordsBn = listOf(setOf("বুক", "ব্যথা"), setOf("শ্বাসকষ্ট")),
            reasonBn = "বুকব্যথা এবং শ্বাসকষ্ট একসাথে হৃদরোগ বা হার্ট অ্যাটাকের মারাত্মক লক্ষণ হতে পারে। এক মুহূর্তও দেরি না করে জরুরি চিকিৎসা নিন বা ৯৯৯ এ কল করুন।",
            urgency = Urgency.IMMEDIATE_999
        ),
        Rule(
            requiredIds = setOf("slurred_speech", "one_sided_weakness"),
            requiredKeywordsBn = listOf(setOf("জড়িয়ে", "কথা"), setOf("অবশ", "প্যারালাইসিস")),
            reasonBn = "মুখ বা একপাশ অবশ হওয়া এবং কথা জড়িয়ে যাওয়া ব্রেন স্ট্রোকের জরুরি লক্ষণ। অবিলম্বে জরুরি হাসপাতালে যান।",
            urgency = Urgency.IMMEDIATE_999
        ),
        Rule(
            requiredIds = setOf("high_fever", "unconsciousness"),
            requiredKeywordsBn = listOf(setOf("জ্বর"), setOf("জ্ঞান", "অচেতন")),
            reasonBn = "তীব্র জ্বরের সাথে জ্ঞান হারানো মারাত্মক মস্তিষ্কের ইনফেকশন বা শকের লক্ষণ।",
            urgency = Urgency.IMMEDIATE_999
        ),
        Rule(
            requiredIds = setOf("severe_headache", "blurred_vision", "vomiting"),
            requiredKeywordsBn = listOf(setOf("মাথাব্যথা"), setOf("ঝাপসা"), setOf("বমি")),
            reasonBn = "তীব্র মাথাব্যথার সাথে চোখে ঝাপসা দেখা এবং বমি মারাত্মক উচ্চ রক্তচাপ বা মস্তিষ্কের চাপের জরুরি লক্ষণ।",
            urgency = Urgency.IMMEDIATE_999
        ),
        Rule(
            requiredIds = setOf("unconsciousness"),
            requiredKeywordsBn = listOf(setOf("জ্ঞান", "অচেতন")),
            reasonBn = "রোগীর চেতনা হারানো বা অচেতন অবস্থা একটি চরম জরুরি মেডিকেল অবস্থা। অবিলম্বে চিকিৎসা সহায়তা প্রয়োজন।",
            urgency = Urgency.IMMEDIATE_999
        )
    )

    /**
     * Executes in 0ms synchronously on-device (Zero network calls - Clinical Guardrail #4).
     */
    operator fun invoke(
        selectedSymptomIds: Set<String>,
        selectedSymptomNames: Set<String> = emptySet(),
        severity: Int = 5
    ): EmergencyTriageResult {
        val lowerNames = selectedSymptomNames.map { it.trim().lowercase() }

        for (rule in rules) {
            val matchesById = rule.requiredIds.isNotEmpty() && selectedSymptomIds.containsAll(rule.requiredIds)
            val matchesByKeyword = rule.requiredKeywordsBn.all { keywordGroup ->
                lowerNames.any { name ->
                    keywordGroup.any { kw -> name.contains(kw) }
                }
            }

            if (matchesById || (rule.requiredKeywordsBn.isNotEmpty() && matchesByKeyword)) {
                return EmergencyTriageResult(
                    isEmergency = true,
                    urgency = rule.urgency,
                    reasonBn = rule.reasonBn,
                    matchedSymptoms = rule.requiredIds
                )
            }
        }

        // Special check: severe chest pain alone with high severity rating (>=8)
        if ((selectedSymptomIds.contains("chest_pain") || lowerNames.any { it.contains("বুক") && it.contains("ব্যথা") }) && severity >= 8) {
            return EmergencyTriageResult(
                isEmergency = true,
                urgency = Urgency.IMMEDIATE_999,
                reasonBn = "তীব্র মাত্রার বুকব্যথা সম্ভাব্য হার্ট অ্যাটাকের লক্ষণ হতে পারে। অনতিবিলম্বে জরুরি চিকিৎসকের শরণাপন্ন হোন।",
                matchedSymptoms = setOf("chest_pain")
            )
        }

        return EmergencyTriageResult(
            isEmergency = false,
            urgency = Urgency.NORMAL,
            reasonBn = "কোনো তাৎক্ষণিক জরুরি বা রেড-ফ্ল্যাগ লক্ষণ পাওয়া যায়নি। স্বাভাবিক নিয়মে চিকিৎসকের পরামর্শ নিন।",
            matchedSymptoms = emptySet()
        )
    }
}
