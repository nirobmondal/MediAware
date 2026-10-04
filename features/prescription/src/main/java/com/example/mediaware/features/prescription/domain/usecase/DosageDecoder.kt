package com.example.mediaware.features.prescription.domain.usecase

object DosageDecoder {

    data class DosageInstruction(
        val timesPerDay: Int,
        val morningQty: Int,
        val noonQty: Int,
        val nightQty: Int,
        val timingSlotBn: String,
        val mealInstructionBn: String
    )

    fun decodeSchedule(pattern: String, mealCode: String?): DosageInstruction {
        // Normalize Bengali digits to English digits for consistent parsing
        val normalizedDigits = pattern.trim().uppercase()
            .replace('০', '0')
            .replace('১', '1')
            .replace('২', '2')
            .replace('৩', '3')
            .replace('৪', '4')
            .replace('-', '+')
            .replace('/', '+')
            .replace(" ", "")

        val mealTiming = when (mealCode?.trim()?.uppercase()?.replace(".", "")) {
            "AC", "খাবার আগে", "খাবারের আগে", "ANTE CIBUM" -> "খাবারের ৩০ মিনিট পূর্বে সেব্য"
            "PC", "খাবার পরে", "খাবার পর", "খাবারের পর", "POST CIBUM" -> "ভরা পেটে বা খাবারের পর সেব্য"
            "WITH_FOOD", "খাবারের সাথে" -> "খাবারের সাথে সেব্য"
            "HS", "BEDTIME", "রাতে শোবার আগে", "শোয়ার আগে" -> "রাতে ঘুমানোর পূর্বে সেব্য"
            else -> "চিকিৎসকের নির্দেশিত সময়ে"
        }

        return when (normalizedDigits) {
            "1+0+0", "OD", "100" -> DosageInstruction(1, 1, 0, 0, "সকালে ১ বার", mealTiming)
            "0+0+1", "001" -> DosageInstruction(1, 0, 0, 1, "রাতে ১ বার", mealTiming)
            "1+0+1", "BD", "BID", "101" -> DosageInstruction(2, 1, 0, 1, "সকালে ও রাতে (দিনে ২ বার)", mealTiming)
            "1+1+1", "TDS", "TID", "111" -> DosageInstruction(3, 1, 1, 1, "সকাল, দুপুর ও রাতে (দিনে ৩ বার)", mealTiming)
            "1+1+1+1", "QDS", "QID" -> DosageInstruction(4, 1, 1, 1, "প্রতি ৬ ঘণ্টা পর (দিনে ৪ বার)", mealTiming)
            "0+1+0", "010" -> DosageInstruction(1, 0, 1, 0, "দুপুরে ১ বার", mealTiming)
            "PRN", "SOS" -> DosageInstruction(0, 0, 0, 0, "প্রয়োজন অনুযায়ী (সমস্যা হলে)", mealTiming)
            else -> {
                // Regex check for custom 3-part patterns like "2+0+1" or "0.5+0+0.5"
                val parts = normalizedDigits.split("+")
                if (parts.size == 3) {
                    val m = parts[0].toIntOrNull() ?: 1
                    val n = parts[1].toIntOrNull() ?: 0
                    val e = parts[2].toIntOrNull() ?: 0
                    val total = (if (m > 0) 1 else 0) + (if (n > 0) 1 else 0) + (if (e > 0) 1 else 0)
                    DosageInstruction(total, m, n, e, "দিনে ${total} বার (প্রেসক্রিপশন অনুযায়ী)", mealTiming)
                } else {
                    DosageInstruction(1, 1, 0, 0, "ডাক্তারের পরামর্শ অনুযায়ী", mealTiming)
                }
            }
        }
    }

    fun parseDurationDays(rawText: String): Int {
        val normalized = rawText.lowercase()
            .replace('০', '0')
            .replace('১', '1')
            .replace('২', '2')
            .replace('৩', '3')
            .replace('৪', '4')
            .replace('৫', '5')
            .replace('৬', '6')
            .replace('৭', '7')
            .replace('৮', '8')
            .replace('৯', '9')

        val regex = Regex("""(\d+)\s*(day|days|d|din|মাস|দিন|সপ্তাহ|month|week|m|w)?""")
        val match = regex.find(normalized) ?: return 7

        val num = match.groupValues[1].toIntOrNull() ?: 7
        val unit = match.groupValues.getOrNull(2) ?: "day"

        return when {
            unit.contains("মাস") || unit.contains("month") || unit == "m" -> num * 30
            unit.contains("সপ্তাহ") || unit.contains("week") || unit == "w" -> num * 7
            else -> num
        }
    }
}
