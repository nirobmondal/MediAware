package com.example.mediaware.features.prescription.domain.usecase

import com.example.mediaware.features.prescription.domain.model.PrescriptionItem
import javax.inject.Inject

class DecodeLatinRxUseCase @Inject constructor() {

    private val commonDrugNames = listOf(
        "Seclo" to "Omeprazole",
        "Omeprazole" to "Omeprazole",
        "Napa" to "Paracetamol",
        "Napa Extra" to "Paracetamol + Caffeine",
        "Ace" to "Paracetamol",
        "Metformin" to "Metformin",
        "Comet" to "Metformin",
        "Gluconor" to "Metformin",
        "Losartan" to "Losartan Potassium",
        "Osartil" to "Losartan Potassium",
        "Angilock" to "Losartan Potassium",
        "Amlodipine" to "Amlodipine",
        "Camlodin" to "Amlodipine",
        "Amodis" to "Metronidazole",
        "Azithromycin" to "Azithromycin",
        "Zimax" to "Azithromycin",
        "Trulimax" to "Azithromycin",
        "Montelukast" to "Montelukast",
        "Monas" to "Montelukast",
        "Pantoprazole" to "Pantoprazole",
        "Trupan" to "Pantoprazole",
        "Esomeprazole" to "Esomeprazole",
        "Maxpro" to "Esomeprazole",
        "Nexum" to "Esomeprazole",
        "Ciprofloxacin" to "Ciprofloxacin",
        "Neofloxin" to "Ciprofloxacin",
        "Ciprocin" to "Ciprofloxacin",
        "Paracetamol" to "Paracetamol"
    )

    operator fun invoke(rawOcrText: String): List<PrescriptionItem> {
        val lines = rawOcrText.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val items = mutableListOf<PrescriptionItem>()

        for (line in lines) {
            val item = parseLine(line)
            if (item != null) {
                items.add(item)
            }
        }

        return items
    }

    fun parseLine(line: String): PrescriptionItem? {
        val upper = line.uppercase()

        // Check if line contains any recognized drug name
        var matchedBrand: String? = null
        var matchedGeneric: String = ""

        for ((brand, generic) in commonDrugNames) {
            if (line.contains(brand, ignoreCase = true)) {
                matchedBrand = brand
                matchedGeneric = generic
                break
            }
        }

        if (matchedBrand == null) {
            // Check if line looks like a drug entry with strength (e.g. Tab. X 500mg 1+0+1)
            val nameRegex = Regex("""(?:Tab\.|Cap\.|Syr\.)?\s*([A-Za-z\u0980-\u09FF]{3,20})""")
            val match = nameRegex.find(line)
            if (match != null && (upper.contains("+") || upper.contains("OD") || upper.contains("BD") || upper.contains("TDS"))) {
                matchedBrand = match.groupValues[1]
                matchedGeneric = matchedBrand
            } else {
                return null
            }
        }

        // Extract strength (e.g. 500mg, 20 mg, 5ml)
        val strengthRegex = Regex("""(\d+\.?\d*)\s*(mg|ml|gm|mcg)""", RegexOption.IGNORE_CASE)
        val strength = strengthRegex.find(line)?.value ?: ""

        // Extract dosage pattern (e.g., 1+0+1, 1-0-1, OD, BD, TDS, ১+০+১)
        val dosageRegex = Regex("""([0-9\u09E6-\u09EF][\+\-][0-9\u09E6-\u09EF][\+\-][0-9\u09E6-\u09EF]|OD|BD|TDS|QDS|PRN|SOS)""", RegexOption.IGNORE_CASE)
        val rawDosage = dosageRegex.find(line)?.value ?: "1+0+1"

        // Extract meal timing (e.g., AC, PC, a.c., p.c., খাবার আগে, খাবার পর)
        val mealRegex = Regex("""(a\.?c\.?|p\.?c\.?|খাবার\s*(?:আগে|পর|পূর্বে|সাথে))""", RegexOption.IGNORE_CASE)
        val rawMeal = mealRegex.find(line)?.value ?: "PC"

        // Extract duration
        val durationDays = DosageDecoder.parseDurationDays(line)

        val instruction = DosageDecoder.decodeSchedule(rawDosage, rawMeal)

        val form = when {
            line.contains("Cap", ignoreCase = true) || line.contains("ক্যাপসুল") -> "ক্যাপসুল"
            line.contains("Syr", ignoreCase = true) || line.contains("সিরাপ") -> "সিরাপ"
            else -> "ট্যাবলেট"
        }

        return PrescriptionItem(
            brandName = matchedBrand,
            genericName = matchedGeneric,
            strength = strength,
            form = form,
            rawDosagePattern = rawDosage,
            rawMealTiming = rawMeal,
            durationDays = durationDays,
            morningQty = instruction.morningQty,
            noonQty = instruction.noonQty,
            nightQty = instruction.nightQty,
            timingSlotBn = instruction.timingSlotBn,
            mealInstructionBn = instruction.mealInstructionBn
        )
    }
}
