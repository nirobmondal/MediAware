package com.example.mediaware.features.report.domain.model

enum class LabStatus(val displayNameBn: String) {
    NORMAL("স্বাভাবিক"),
    BORDERLINE("সতর্কসীমা (বর্ডারলাইন)"),
    CRITICAL("উচ্চ বা ঝুঁকিপূর্ণ")
}

data class ExtractedLabItem(
    val id: String,
    val key: String,
    val testNameBn: String,
    val testNameEn: String,
    var numericValue: Double,
    val unit: String,
    val normalMin: Double,
    val normalMax: Double,
    val criticalThreshold: Double,
    val status: LabStatus = LabStatus.NORMAL,
    val clinicalExplanationBn: String = "",
    val isFromSmartCache: Boolean = true
)

data class DoctorQuestion(
    val id: String,
    val testNameBn: String,
    val questionBn: String,
    val rationaleBn: String,
    val isChecked: Boolean = false
)

data class LabReportAnalysis(
    val reportDateFormattedBn: String,
    val items: List<ExtractedLabItem>,
    val overallSummaryBn: String,
    val criticalCount: Int,
    val borderlineCount: Int,
    val normalCount: Int,
    val mandatoryDisclaimerBn: String = "⚠️ এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।"
)
