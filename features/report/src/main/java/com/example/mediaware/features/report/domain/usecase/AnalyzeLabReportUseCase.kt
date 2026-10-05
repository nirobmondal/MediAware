package com.example.mediaware.features.report.domain.usecase

import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.report.domain.model.ExtractedLabItem
import com.example.mediaware.features.report.domain.model.LabReportAnalysis
import com.example.mediaware.features.report.domain.model.LabStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class AnalyzeLabReportUseCase @Inject constructor() {

    operator fun invoke(items: List<ExtractedLabItem>): LabReportAnalysis {
        val evaluatedItems = items.map { item ->
            evaluateItem(item)
        }

        val criticalCount = evaluatedItems.count { it.status == LabStatus.CRITICAL }
        val borderlineCount = evaluatedItems.count { it.status == LabStatus.BORDERLINE }
        val normalCount = evaluatedItems.count { it.status == LabStatus.NORMAL }

        val summary = buildString {
            if (criticalCount > 0) {
                append("${criticalCount.toString().toBengaliDigits()}টি টেস্টে মান উচ্চ/ঝুঁকিপূর্ণ পাওয়া গেছে। অনতিবিলম্বে চিকিৎসকের সাথে যোগাযোগ করুন। ")
            }
            if (borderlineCount > 0) {
                append("${borderlineCount.toString().toBengaliDigits()}টি টেস্টের মান সতর্কসীমায় রয়েছে। খাদ্যাভ্যাস ও জীবনযাত্রায় নজর দেওয়া প্রয়োজন। ")
            }
            if (criticalCount == 0 && borderlineCount == 0) {
                append("পরীক্ষিত সকল উপাদানের মান স্বাভাবিক রেফারেন্স সীমার মধ্যে রয়েছে।")
            }
        }

        val dateFormat = SimpleDateFormat("dd MMMM, yyyy", Locale("bn", "BD"))
        val formattedDate = dateFormat.format(Date()).toBengaliDigits()

        return LabReportAnalysis(
            reportDateFormattedBn = formattedDate,
            items = evaluatedItems,
            overallSummaryBn = summary,
            criticalCount = criticalCount,
            borderlineCount = borderlineCount,
            normalCount = normalCount,
            mandatoryDisclaimerBn = "এটি কোনো প্রেসক্রিপশন নয়। যেকোনো সিদ্ধান্তে ডাক্তারের পরামর্শ নিন।"
        )
    }

    private fun evaluateItem(item: ExtractedLabItem): ExtractedLabItem {
        val v = item.numericValue

        return when (item.key) {
            "fbs" -> {
                val status = when {
                    v <= item.normalMax -> LabStatus.NORMAL
                    v < item.criticalThreshold -> LabStatus.BORDERLINE
                    else -> LabStatus.CRITICAL
                }
                val explanation = when (status) {
                    LabStatus.NORMAL -> "রক্তে সুগারের স্বাভাবিক মাত্রা হলো ১০০ mg/dL-এর নিচে। আপনার মান স্বাভাবিক সীমার মধ্যে আছে।"
                    LabStatus.BORDERLINE -> "রক্তে সুগারের মাত্রা স্বাভাবিকের চেয়ে কিছুটা বেশি দেখাচ্ছে (ইমপেয়ার্ড ফাস্টিং গ্লুকোজ)। মিষ্টি ও অতিরিক্ত শর্করা নিয়ন্ত্রণ করে চিকিৎসকের পরামর্শ নিন।"
                    LabStatus.CRITICAL -> "রক্তে সুগারের মাত্রা উল্লেখযোগ্যভাবে বেশি নির্দেশ করছে। কোনো রোগ নিজে ডায়াগনসিস না করে অতি দ্রুত চিকিৎসকের সাথে যোগাযোগ করে পূর্ণাঙ্গ ডায়াবেটিস মূল্যায়ন করান।"
                }
                item.copy(status = status, clinicalExplanationBn = explanation)
            }

            "rbs" -> {
                val status = when {
                    v <= item.normalMax -> LabStatus.NORMAL
                    v < item.criticalThreshold -> LabStatus.BORDERLINE
                    else -> LabStatus.CRITICAL
                }
                val explanation = when (status) {
                    LabStatus.NORMAL -> "র‌্যান্ডম সুগার স্বাভাবিক মাত্রার মধ্যে রয়েছে (১৪০ mg/dL-এর কম)।"
                    LabStatus.BORDERLINE -> "র‌্যান্ডম সুগার কিছুটা বেশি নির্দেশ করছে। সঠিক মূল্যায়নে ফাস্টিং টেস্টের জন্য চিকিৎসকের পরামর্শ নিন।"
                    LabStatus.CRITICAL -> "র‌্যান্ডম সুগার ২০০ mg/dL বা তার বেশি দেখাচ্ছে। এটি সতর্কতামূলক সংকেত, চিকিৎসকের পরামর্শ নেওয়া আবশ্যক।"
                }
                item.copy(status = status, clinicalExplanationBn = explanation)
            }

            "creatinine" -> {
                val status = when {
                    v <= item.normalMax -> LabStatus.NORMAL
                    v <= item.criticalThreshold -> LabStatus.BORDERLINE
                    else -> LabStatus.CRITICAL
                }
                val explanation = when (status) {
                    LabStatus.NORMAL -> "সিরাম ক্রিয়েটিনিনের মাত্রা সন্তোষজনক এবং স্বাভাবিক কিডনি ফিল্ট্রেশন সীমার মধ্যে রয়েছে।"
                    LabStatus.BORDERLINE -> "ক্রিয়েটিনিনের মাত্রা সামান্য বেশি নির্দেশ করছে, যা সাময়িক পানিশূন্যতা বা কিডনি চাপের লক্ষণ হতে পারে। পর্যাপ্ত পানি পান করুন ও চিকিৎসকের পরামর্শ নিন।"
                    LabStatus.CRITICAL -> "ক্রিয়েটিনিনের মাত্রা স্বাভাবিকের চেয়ে বেশ বেশি পাওয়া গেছে। কোনো ব্যথানাশক ওষুধ খাওয়া বন্ধ রাখুন এবং অবিলম্বে কিডনি বা মেডিসিন বিশেষজ্ঞের পরামর্শ নিন।"
                }
                item.copy(status = status, clinicalExplanationBn = explanation)
            }

            "hemoglobin" -> {
                val status = when {
                    v < item.criticalThreshold -> LabStatus.CRITICAL
                    v < item.normalMin -> LabStatus.BORDERLINE
                    else -> LabStatus.NORMAL
                }
                val explanation = when (status) {
                    LabStatus.NORMAL -> "রক্তে হিমোগ্লোবিনের মাত্রা সন্তোষজনক ও স্বাভাবিক।"
                    LabStatus.BORDERLINE -> "হিমোগ্লোবিনের মাত্রা কিছুটা কম দেখাচ্ছে। আয়রন ও পুষ্টিকর খাবার গ্রহণ বাড়ানো এবং ডাক্তারের পরামর্শ নেওয়া ভালো।"
                    LabStatus.CRITICAL -> "হিমোগ্লোবিনের মাত্রা বেশ কম (১০-এর নিচে), যা তীব্র রক্তশূন্যতার লক্ষণ হতে পারে। চিকিৎসকের শরণাপন্ন হয়ে কারণ নির্ণয় করুন।"
                }
                item.copy(status = status, clinicalExplanationBn = explanation)
            }

            "hba1c" -> {
                val status = when {
                    v <= item.normalMax -> LabStatus.NORMAL
                    v < item.criticalThreshold -> LabStatus.BORDERLINE
                    else -> LabStatus.CRITICAL
                }
                val explanation = when (status) {
                    LabStatus.NORMAL -> "বিগত ৩ মাসের গড় সুগারের মাত্রা সম্পূর্ণ স্বাভাবিক সীমার মধ্যে আছে (৫.৭% এর নিচে)।"
                    LabStatus.BORDERLINE -> "বিগত ৩ মাসের সুগারের গড় মান কিছুটা বেশি নির্দেশ করছে (প্রি-ডায়াবেটিস রেঞ্জ)। খাদ্য নিয়ন্ত্রণ প্রয়োজন।"
                    LabStatus.CRITICAL -> "বিগত ৩ মাসের গড় সুগার ৬.৫% বা তার বেশি পাওয়া গেছে। বিশেষজ্ঞ চিকিৎসকের পরামর্শ অনুযায়ী দীর্ঘমেয়াদি পরিকল্পনা গ্রহণ করুন।"
                }
                item.copy(status = status, clinicalExplanationBn = explanation)
            }

            "cholesterol" -> {
                val status = when {
                    v <= item.normalMax -> LabStatus.NORMAL
                    v < item.criticalThreshold -> LabStatus.BORDERLINE
                    else -> LabStatus.CRITICAL
                }
                val explanation = when (status) {
                    LabStatus.NORMAL -> "টোটাল কোলেস্টেরলের মাত্রা স্বাভাবিক হৃদবান্ধব সীমার মধ্যে রয়েছে (২০০ mg/dL-এর কম)।"
                    LabStatus.BORDERLINE -> "কোলেস্টেরলের মাত্রা বর্ডারলাইন উচ্চ নির্দেশ করছে। তৈলাক্ত খাবার কমান ও নিয়মিত হাঁটাচলা বজায় রাখুন।"
                    LabStatus.CRITICAL -> "কোলেস্টেরলের মাত্রা উচ্চ ঝুঁকিপূর্ণ সীমার উপরে রয়েছে। চিকিৎসকের সাথে কথা বলে লিপিড প্রোফাইল পর্যালোচনা করান।"
                }
                item.copy(status = status, clinicalExplanationBn = explanation)
            }

            else -> {
                val status = when {
                    v <= item.normalMax -> LabStatus.NORMAL
                    v <= item.criticalThreshold -> LabStatus.BORDERLINE
                    else -> LabStatus.CRITICAL
                }
                val explanation = "মানটি পরীক্ষাগারের রেফারেন্স সীমার সাথে তুলনা করা হয়েছে। বিস্তারিত মূল্যায়নে ডাক্তারের পরামর্শ নিন।"
                item.copy(status = status, clinicalExplanationBn = explanation)
            }
        }
    }
}
