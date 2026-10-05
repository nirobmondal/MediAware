package com.example.mediaware.features.report.domain.usecase

import com.example.mediaware.core.designsystem.util.toEnglishDigits
import com.example.mediaware.features.report.domain.model.ExtractedLabItem
import com.example.mediaware.features.report.domain.model.LabStatus
import java.util.UUID
import javax.inject.Inject

class NormalizeOcrUseCase @Inject constructor() {

    private data class PatternDef(
        val key: String,
        val testNameBn: String,
        val testNameEn: String,
        val aliases: List<String>,
        val unit: String,
        val normalMin: Double,
        val normalMax: Double,
        val criticalThreshold: Double
    )

    private val supportedPatterns = listOf(
        PatternDef(
            key = "fbs",
            testNameBn = "ফাস্টিং ব্লাড সুগার (FBS)",
            testNameEn = "Fasting Blood Sugar",
            aliases = listOf("fasting", "fbs", "glucose fasting", "sugar fasting", "blood sugar fasting"),
            unit = "mg/dL",
            normalMin = 70.0,
            normalMax = 99.0,
            criticalThreshold = 126.0
        ),
        PatternDef(
            key = "rbs",
            testNameBn = "র‌্যান্ডম ব্লাড সুগার (RBS)",
            testNameEn = "Random Blood Sugar",
            aliases = listOf("random", "rbs", "glucose random", "sugar random"),
            unit = "mg/dL",
            normalMin = 70.0,
            normalMax = 139.0,
            criticalThreshold = 200.0
        ),
        PatternDef(
            key = "creatinine",
            testNameBn = "সিরাম ক্রিয়েটিনিন (Serum Creatinine)",
            testNameEn = "Serum Creatinine",
            aliases = listOf("creatinine", "serum creatinine", "creat", "s.creatinine"),
            unit = "mg/dL",
            normalMin = 0.6,
            normalMax = 1.2,
            criticalThreshold = 1.5
        ),
        PatternDef(
            key = "hemoglobin",
            testNameBn = "হিমোগ্লোবিন (Hemoglobin)",
            testNameEn = "Hemoglobin",
            aliases = listOf("hemoglobin", "hb", "hgb", "haemoglobin"),
            unit = "g/dL",
            normalMin = 12.0,
            normalMax = 16.5,
            criticalThreshold = 10.0
        ),
        PatternDef(
            key = "hba1c",
            testNameBn = "এইচবিএ১সি (HbA1c)",
            testNameEn = "HbA1c",
            aliases = listOf("hba1c", "glycated hemoglobin", "a1c"),
            unit = "%",
            normalMin = 4.0,
            normalMax = 5.6,
            criticalThreshold = 6.5
        ),
        PatternDef(
            key = "cholesterol",
            testNameBn = "টোটাল কোলেস্টেরল (Total Cholesterol)",
            testNameEn = "Total Cholesterol",
            aliases = listOf("cholesterol", "total cholesterol", "chol", "s.cholesterol"),
            unit = "mg/dL",
            normalMin = 100.0,
            normalMax = 199.0,
            criticalThreshold = 240.0
        )
    )

    operator fun invoke(rawText: String): List<ExtractedLabItem> {
        val normalized = rawText.toEnglishDigits().lowercase()
        val lines = normalized.lines()
        val matchedItems = mutableListOf<ExtractedLabItem>()
        val foundKeys = mutableSetOf<String>()

        for (pattern in supportedPatterns) {
            if (foundKeys.contains(pattern.key)) continue

            for (line in lines) {
                val matchesAlias = pattern.aliases.any { alias -> line.contains(alias) }
                if (matchesAlias) {
                    val numberMatch = Regex("""\b(\d+(\.\d+)?)\b""").find(line)
                    if (numberMatch != null) {
                        val value = numberMatch.groupValues[1].toDoubleOrNull()
                        if (value != null && value > 0.0) {
                            foundKeys.add(pattern.key)
                            matchedItems.add(
                                ExtractedLabItem(
                                    id = UUID.randomUUID().toString(),
                                    key = pattern.key,
                                    testNameBn = pattern.testNameBn,
                                    testNameEn = pattern.testNameEn,
                                    numericValue = value,
                                    unit = pattern.unit,
                                    normalMin = pattern.normalMin,
                                    normalMax = pattern.normalMax,
                                    criticalThreshold = pattern.criticalThreshold
                                )
                            )
                            break
                        }
                    }
                }
            }
        }
        return matchedItems
    }
}
