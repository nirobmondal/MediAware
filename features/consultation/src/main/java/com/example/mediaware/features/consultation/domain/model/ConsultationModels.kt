package com.example.mediaware.features.consultation.domain.model

enum class ActionItemCategory(val labelBn: String) {
    MEDICATION("ওষুধ"),
    TEST("ল্যাব টেস্ট"),
    LIFESTYLE("খাদ্যাভ্যাস ও ব্যায়াম"),
    GENERAL("সাধারণ নির্দেশনা")
}

data class ConsultationActionItem(
    val id: String,
    val task: String,
    val category: ActionItemCategory,
    val isCompleted: Boolean = false
)

data class ConsultationSummary(
    val id: String,
    val doctorName: String,
    val visitDateBn: String,
    val summaryBn: String,
    val actionItems: List<ConsultationActionItem>,
    val pendingQuestions: List<String>,
    val followUpDays: Int,
    val followUpDateStringBn: String,
    val followUpReasonBn: String,
    val disclaimerBn: String = "এটি কোনো প্রেসক্রিপশন নয়। শুধুমাত্র ডাক্তারের পরামর্শের সারাংশ। যেকোনো সিদ্ধান্তে ডাক্তারের সাথে কথা বলুন।"
)
