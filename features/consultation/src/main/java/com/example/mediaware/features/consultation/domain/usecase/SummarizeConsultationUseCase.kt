package com.example.mediaware.features.consultation.domain.usecase

import com.example.mediaware.features.consultation.domain.model.ActionItemCategory
import com.example.mediaware.features.consultation.domain.model.ConsultationActionItem
import com.example.mediaware.features.consultation.domain.model.ConsultationSummary
import javax.inject.Inject

/**
 * Pure Kotlin UseCase for extracting structured post-consultation intelligence.
 *
 * Enforces Clinical Safety Guardrails:
 * - Guardrail #1 (Never Diagnose): Observational guidance only.
 * - Guardrail #2 (Never Alter Dosages): Re-iterates prescribed regimens without alteration.
 * - Guardrail #3 (Mandatory Deferral Disclaimer): Attaches prominent deferral disclaimer.
 * - Guardrail #4 (0ms Offline Safety): Fully executes on-device synchronously without network dependency.
 */
class SummarizeConsultationUseCase @Inject constructor() {

    operator fun invoke(
        transcript: String? = null,
        consultationNotes: String? = null
    ): ConsultationSummary? {
        val text = transcript?.takeIf { it.isNotBlank() } ?: consultationNotes?.takeIf { it.isNotBlank() }
        return if (!text.isNullOrBlank()) {
            extractFromText(text)
        } else {
            null
        }
    }

    private fun extractFromText(text: String): ConsultationSummary? {
        val actionItems = mutableListOf<ConsultationActionItem>()
        var itemId = 1

        val lower = text.lowercase()

        // 1. Medication action items extraction
        if (lower.contains("মেটফরমিন") || lower.contains("metformin") || lower.contains("ওষুধ") || lower.contains("সেবন")) {
            actionItems.add(
                ConsultationActionItem(
                    id = "act_${itemId++}",
                    task = "প্রেসক্রিপশন অনুযায়ী নিয়মিত ওষুধ সেবন চালিয়ে যান",
                    category = ActionItemCategory.MEDICATION,
                    isCompleted = false
                )
            )
        }

        // 2. Lab tests action items extraction
        if (lower.contains("টেস্ট") || lower.contains("test") || lower.contains("hba1c") || lower.contains("রক্ত") || lower.contains("সুগার")) {
            actionItems.add(
                ConsultationActionItem(
                    id = "act_${itemId++}",
                    task = "ডাক্তারের পরামর্শ অনুযায়ী নির্দিষ্ট ল্যাব পরীক্ষা সম্পন্ন করুন",
                    category = ActionItemCategory.TEST,
                    isCompleted = false
                )
            )
        }

        // 3. Lifestyle action items extraction
        if (lower.contains("হাঁটা") || lower.contains("ব্যায়াম") || lower.contains("খাবার") || lower.contains("মিষ্টি") || lower.contains("চর্বি")) {
            actionItems.add(
                ConsultationActionItem(
                    id = "act_${itemId++}",
                    task = "প্রতিদিন পরিমিত শারীরিক ব্যায়াম বা নিয়মিত হাঁটার অভ্যাস বজায় রাখুন",
                    category = ActionItemCategory.LIFESTYLE,
                    isCompleted = false
                )
            )
            actionItems.add(
                ConsultationActionItem(
                    id = "act_${itemId++}",
                    task = "অতিরিক্ত মিষ্টি ও তৈলাক্ত খাবার খাওয়া পরিহার করুন",
                    category = ActionItemCategory.LIFESTYLE,
                    isCompleted = false
                )
            )
        }

        if (actionItems.isEmpty()) {
            return null
        }

        return ConsultationSummary(
            id = "consult_summary_${System.currentTimeMillis()}",
            doctorName = "চিকিৎসক",
            visitDateBn = "পরামর্শ সারাংশ",
            summaryBn = "প্রেসক্রিপশন ও ডাক্তারের পরামর্শ অনুযায়ী নিয়মিত ওষুধ সেবন করুন এবং নির্দেশিত নিয়ম মেনে চলুন।",
            actionItems = actionItems,
            pendingQuestions = emptyList(),
            followUpDays = 30,
            followUpDateStringBn = "১ মাস পর (৩০ দিন)",
            followUpReasonBn = "ডাক্তারের পরামর্শ পর্যালোচনা ও ফলো-আপ"
        )
    }
}
