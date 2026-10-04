package com.example.mediaware.features.symptom.domain.usecase

import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.core.model.UserProfile
import com.example.mediaware.features.symptom.domain.model.Symptom
import com.example.mediaware.features.symptom.domain.model.VisitPrepCard
import javax.inject.Inject

class GenerateVisitPrepUseCase @Inject constructor() {

    operator fun invoke(
        userProfile: UserProfile?,
        symptoms: List<Symptom>,
        severityRating: Int,
        durationBn: String
    ): VisitPrepCard {
        val age = userProfile?.age ?: 40
        val genderBn = userProfile?.gender?.displayNameBn ?: "উল্লেখ নেই"
        val chronicConditions = userProfile?.chronicConditions ?: emptyList()

        val demographicsSummary = buildString {
            append("বয়স: ${age.toString().toBengaliDigits()} বছর | লিঙ্গ: $genderBn")
            if (chronicConditions.isNotEmpty()) {
                append(" | পূর্ববর্তী জটিলতা: ${chronicConditions.joinToString(", ")}")
            }
        }

        val suggestedSpecialist = determineSpecialist(symptoms, chronicConditions)

        val symptomsListBn = symptoms.joinToString(", ") { it.nameBn }
        val chiefComplaints = if (symptomsListBn.isNotBlank()) {
            "প্রধান সমস্যা: $symptomsListBn (স্থায়িত্বকাল: $durationBn, তীব্রতা: ${severityRating.toString().toBengaliDigits()}/১০)"
        } else {
            "লক্ষণ পর্যালোচনাধীন (স্থায়িত্বকাল: $durationBn)"
        }

        // 3 concise bullet points for 30-second chamber presentation
        val speakingPoints = mutableListOf<String>()
        speakingPoints.add("১. সমস্যা শুরু হয়েছে $durationBn আগে, বর্তমানে অনুভূত তীব্রতা ১০ এর মধ্যে ${severityRating.toString().toBengaliDigits()}।")
        speakingPoints.add("২. মূল অনুভূত লক্ষণসমূহ: $symptomsListBn। দৈনন্দিন স্বাভাবিক কার্যক্রমে কিছুটা প্রভাব ফেলছে।")
        if (chronicConditions.isNotEmpty()) {
            speakingPoints.add("৩. পূর্ব ইতিহাস: রোগী আগে থেকেই ${chronicConditions.joinToString(", ")} রোগে ভুগছেন এবং নিয়মিত ফলোআপে আছেন।")
        } else {
            speakingPoints.add("৩. রোগী অতীতে গুরুতর কোনো দীর্ঘমেয়াদি রোগে আক্রান্ত হননি।")
        }

        // Targeted doctor questions aligned with DGHS guidelines
        val doctorQuestions = mutableListOf<String>()
        doctorQuestions.add("১. এই লক্ষণগুলোর সঠিক কারণ নির্ণয়ে আমার কি কোনো রক্ত বা ল্যাব টেস্ট করানো প্রয়োজন?")
        doctorQuestions.add("২. লক্ষণগুলো উপশমে আমার খাদ্যাভ্যাস বা জীবনযাত্রায় তাৎক্ষণিক কোনো পরিবর্তন আনা জরুরি কি?")
        doctorQuestions.add("৩. কোনো সতর্কতামূলক লক্ষণ দেখা দিলে আমাকে দ্রুত আবার আসতে হবে?")

        return VisitPrepCard(
            demographicsSummaryBn = demographicsSummary,
            suggestedSpecialistBn = suggestedSpecialist,
            severityScaleBn = "${severityRating.toString().toBengaliDigits()} / ১০",
            durationBn = durationBn,
            chiefComplaintsSummaryBn = chiefComplaints,
            doctorSpeakingPointsBn = speakingPoints,
            doctorQuestionsBn = doctorQuestions,
            mandatoryDisclaimerBn = "⚠️ এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।"
        )
    }

    private fun determineSpecialist(symptoms: List<Symptom>, chronicConditions: List<String>): String {
        val symptomIds = symptoms.map { it.id }.toSet()

        return when {
            symptomIds.contains("chest_pain") || symptomIds.contains("palpitations") || chronicConditions.any { it.contains("হৃদরোগ") } -> {
                "হৃদরোগ বিশেষজ্ঞ (Cardiologist)"
            }
            symptomIds.contains("slurred_speech") || symptomIds.contains("one_sided_weakness") || symptomIds.contains("unconsciousness") -> {
                "স্নায়ুরোগ বা নিউরোলজি বিশেষজ্ঞ (Neurologist)"
            }
            symptomIds.contains("breathlessness") || symptomIds.contains("cough") || chronicConditions.any { it.contains("অ্যাজমা") } -> {
                "বক্ষব্যাধি বিশেষজ্ঞ (Pulmonologist / Chest Specialist)"
            }
            symptomIds.contains("urinary_burning") || symptomIds.contains("leg_swelling") || chronicConditions.any { it.contains("কিডনি") } -> {
                "কিডনি রোগ বিশেষজ্ঞ (Nephrologist) / ইউরোলজিস্ট"
            }
            symptomIds.contains("vomiting") || symptomIds.contains("abdominal_pain") -> {
                "গ্যাস্ট্রোএন্টারোলজি / পরিপাকতন্ত্র বিশেষজ্ঞ"
            }
            symptomIds.contains("headache") || symptomIds.contains("severe_headache") || symptomIds.contains("blurred_vision") -> {
                "মেডিসিন বা নিউরো-মেডিসিন বিশেষজ্ঞ"
            }
            else -> {
                "জেনারেল মেডিসিন বিশেষজ্ঞ (Internal Medicine Specialist)"
            }
        }
    }
}
