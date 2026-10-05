package com.example.mediaware.features.prescription.data.repository

import com.example.mediaware.core.common.di.IoDispatcher
import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.core.database.dao.MedicineCacheDao
import com.example.mediaware.core.database.entity.MedicineCacheEntity
import com.example.mediaware.features.prescription.domain.model.MedicineExplanation
import com.example.mediaware.features.prescription.domain.repository.MedicineRepository
import kotlinx.coroutines.CoroutineDispatcher
import com.example.mediaware.core.common.ai.GeminiAiClient
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MedicineRepositoryImpl @Inject constructor(
    private val cacheDao: MedicineCacheDao,
    private val geminiAiClient: GeminiAiClient,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MedicineRepository {

    // 180-day TTL duration in milliseconds
    private val ttlDurationMs = TimeUnit.DAYS.toMillis(180)

    // DGHS / Bangladesh Essential Drugs standard clinical knowledge base
    private val localKnowledgeBase = mapOf(
        "omeprazole" to MedicineExplanation(
            genericName = "Omeprazole",
            brandName = "Seclo / Omecon",
            banglaName = "ওমেপ্রাজল",
            therapeuticClass = "প্রোটন পাম্প ইনহিবিটর (PPI / গ্যাস্ট্রিক অ্যান্টাসিড)",
            primaryPurposeBn = "পাকস্থলীতে অতিরিক্ত এসিড উৎপাদন কমায়, গ্যাস্ট্রিক ও পেপটিক আলসারের জ্বালাপোড়া উপশম করে।",
            standardDosageBn = "সাধারণত ২০mg দিনে ১ বার (সকালে নাস্তার ৩০ মিনিট আগে)।",
            sideEffectsBn = "হালকা পেট ব্যথা, ডায়রিয়া বা কোষ্ঠকাঠিন্য হতে পারে।",
            criticalWarningsBn = "খাবারের ৩০ মিনিট পূর্বে সেবন করুন। দীর্ঘমেয়াদে চিকিৎসকের পরামর্শ ছাড়া খাবেন না।",
            lifestylePrecautionsBn = "তৈলাক্ত ও অতিরিক্ত মশলাযুক্ত খাবার পরিহার করুন এবং পর্যাপ্ত পানি পান করুন।"
        ),
        "metformin" to MedicineExplanation(
            genericName = "Metformin",
            brandName = "Comet / Gluconor",
            banglaName = "মেটফরমিন",
            therapeuticClass = "ওরাল অ্যান্টি-ডায়াবেটিক (বিগুয়ানাইড)",
            primaryPurposeBn = "রক্তে সুগারের মাত্রা স্বাভাবিক সীমার মধ্যে রাখতে শরীরকে ইনসুলিন ব্যবহারে সহায়তা করে।",
            standardDosageBn = "সাধারণত ৫০০mg দিনে ১ থেকে ২ বার খাবারের সাথে বা পরে।",
            sideEffectsBn = "প্রাথমিক অবস্থায় পেটে অস্বস্তি, ডায়রিয়া বা বমি বমি ভাব হতে পারে।",
            criticalWarningsBn = "খালি পেটে খাবেন না। চিকিৎসকের পরামর্শ ব্যতীত মাত্রা পরিবর্তন বা বন্ধ করবেন না।",
            lifestylePrecautionsBn = "মিষ্টি ও অতিরিক্ত শর্করা জাতীয় খাবার এড়িয়ে চলুন এবং প্রতিদিন হালকা হাঁটাচলা করুন।"
        ),
        "losartan" to MedicineExplanation(
            genericName = "Losartan Potassium",
            brandName = "Osartil / Angilock",
            banglaName = "লোসারটান পটাশিয়াম",
            therapeuticClass = "অ্যান্টি-হাইপারটেনসিভ (ARB / উচ্চ রক্তচাপের ওষুধ)",
            primaryPurposeBn = "রক্তনালী শিথিল করে উচ্চ রক্তচাপ নিয়ন্ত্রণ করে এবং কিডনি সুরক্ষায় সহায়তা করে।",
            standardDosageBn = "সাধারণত ৫০mg দিনে ১ বার (সকালে বা নির্দিষ্ট সময়ে)।",
            sideEffectsBn = "মাথা ঘোরা, দুর্বলতা বা ক্লান্তিভাব দেখা দিতে পারে।",
            criticalWarningsBn = "হঠাৎ করে ওষুধ সেবন বন্ধ করবেন না। রক্তচাপ স্বাভাবিক থাকলেও নিয়মিত চালিয়ে যান।",
            lifestylePrecautionsBn = "খাবারে বাড়তি কাঁচা লবণ সম্পূর্ণ পরিহার করুন এবং ধূমপান থেকে বিরত থাকুন।"
        ),
        "paracetamol" to MedicineExplanation(
            genericName = "Paracetamol",
            brandName = "Napa / Ace / Fast",
            banglaName = "প্যারাসিটামল",
            therapeuticClass = "অ্যানালজেসিক ও অ্যান্টিপাইরেটিক (ব্যথা ও জ্বর নাশক)",
            primaryPurposeBn = "শারীরিক তাপমাত্রা হ্রাস করে জ্বর নিয়ন্ত্রণ এবং সাধারণ মাথাব্যথা ও গা-ব্যথা কমায়।",
            standardDosageBn = "সাধারণত ৫০০mg ১ থেকে ২টি ট্যাবলেট দিনে ৩ বার (প্রয়োজনে ভরা পেটে)।",
            sideEffectsBn = "সাধারণত নিরাপদ; তবে অতিরিক্ত সেবনে লিভারের ক্ষতি হতে পারে।",
            criticalWarningsBn = "একটানা ৩ দিনের বেশি তীব্র জ্বর থাকলে অবশ্যই চিকিৎসকের পরামর্শ নিন। অতিরিক্ত প্যারাসিটামল সেবন বর্জন করুন।",
            lifestylePrecautionsBn = "জ্বরের সময় প্রচুর তরল খাবার ও পানি পান করুন এবং বিশ্রাম নিন।"
        ),
        "azithromycin" to MedicineExplanation(
            genericName = "Azithromycin",
            brandName = "Zimax / Trulimax",
            banglaName = "অ্যাজিথ্রোমাইসিন",
            therapeuticClass = "ম্যাক্রোলাইড অ্যান্টিবায়োটিক",
            primaryPurposeBn = "শ্বাসতন্ত্র, ফুসফুস ও গলার ব্যাকটেরিয়াল ইনফেকশন বা সংক্রমণ নিরাময় করে।",
            standardDosageBn = "সাধারণত ৫০০mg দিনে ১ বার (খাবারের ১ ঘণ্টা আগে বা ২ ঘণ্টা পরে)।",
            sideEffectsBn = "পেট কামড়ানো, হালকা ডায়রিয়া বা বমি ভাব হতে পারে।",
            criticalWarningsBn = "চিকিৎসকের দেওয়া সম্পূর্ণ কোর্স শেষ করুন। ভালো বোধ করলেও কোর্স অসম্পূর্ণ রাখবেন না।",
            lifestylePrecautionsBn = "ওষুধ সেবনের সময়ে পর্যাপ্ত পানি ও সুষম খাবার গ্রহণ করুন।"
        ),
        "montelukast" to MedicineExplanation(
            genericName = "Montelukast",
            brandName = "Monas / Odmon",
            banglaName = "মন্টেলুকাস্ট",
            therapeuticClass = "অ্যান্টি-অ্যাজমাটিক (লিউકોટ্রাইন রিসেপ্টর অ্যান্টাগনিস্ট)",
            primaryPurposeBn = "শ্বাসনালীর প্রদাহ ও অ্যালার্জি প্রতিরোধ করে শ্বাসকষ্ট ও দীর্ঘমেয়াদী কাশি লাঘব করে।",
            standardDosageBn = "সাধারণত ১০mg ট্যাবলেট দিনে ১ বার রাতে ঘুমানোর আগে।",
            sideEffectsBn = "মাথাব্যথা, ঘুমের মধ্যে দুঃস্বপ্ন বা পেটে অস্বস্তি হতে পারে।",
            criticalWarningsBn = "এটি আকস্মিক তীব্র শ্বাসকষ্টে তাৎক্ষণিক উপশমকারী নয়; নিয়মিত প্রতিরোধী হিসেবে সেব্য।",
            lifestylePrecautionsBn = "ধুলোবালি, ঠান্ডা বাতাস ও অ্যালার্জেনযুক্ত পরিবেশ থেকে নিরাপদ থাকুন।"
        ),
        "amlodipine" to MedicineExplanation(
            genericName = "Amlodipine",
            brandName = "Camlodin / Amdocal",
            banglaName = "অ্যামলোডিপিন",
            therapeuticClass = "ক্যালসিয়াম চ্যানেল ব্লকার (উচ্চ রক্তচাপ নিয়ন্ত্রণ)",
            primaryPurposeBn = "রক্তনালী প্রসারিত করে রক্ত সঞ্চালন সহজ করে এবং রক্তচাপ কমায়।",
            standardDosageBn = "সাধারণত ৫mg দিনে ১ বার নির্দিষ্ট সময়ে।",
            sideEffectsBn = "পায়ে বা গোড়ালিতে হালকা ফোলাভাব ও মুখমণ্ডল লালচে হতে পারে।",
            criticalWarningsBn = "চিকিৎসকের অনুমতি ব্যতীত ডোজ পরিবর্তন করবেন না।",
            lifestylePrecautionsBn = "পা ঝুলিয়ে দীর্ঘক্ষণ বসে থাকা পরিহার করুন এবং রক্তচাপ ট্র্যাক করুন।"
        )
    )

    override suspend fun getMedicineExplanation(genericOrBrand: String): Resource<MedicineExplanation> =
        withContext(ioDispatcher) {
            val normalized = normalizeDrugName(genericOrBrand)
            val now = System.currentTimeMillis()

            // Step 1: Check Local Room Smart Cache (180-day TTL)
            val cached = cacheDao.getCachedMedicine(normalized)
            if (cached != null && cached.ttl_timestamp > now) {
                cacheDao.incrementHitCount(normalized)
                return@withContext Resource.Success(
                    data = MedicineExplanation(
                        genericName = cached.generic_name_normalized.replaceFirstChar { it.uppercase() },
                        brandName = genericOrBrand,
                        banglaName = cached.bangla_name,
                        therapeuticClass = cached.therapeutic_class,
                        primaryPurposeBn = cached.primary_purpose_bn,
                        standardDosageBn = cached.standard_dosage_bn,
                        sideEffectsBn = cached.common_side_effects_bn,
                        criticalWarningsBn = cached.critical_warnings_bn,
                        lifestylePrecautionsBn = cached.lifestyle_precautions_bn,
                        isFromCache = true
                    ),
                    isFromCache = true
                )
            }

            // Step 2: Cache MISS - Check verified clinical knowledge base
            val baseMatch = localKnowledgeBase[normalized]
                ?: localKnowledgeBase.entries.firstOrNull { normalized.contains(it.key) || it.key.contains(normalized) }?.value

            val aiExplanation = geminiAiClient.explainMedicine(
                name = genericOrBrand,
                genericName = baseMatch?.genericName,
                strength = null,
                dosagePattern = baseMatch?.standardDosageBn,
                timing = null
            )

            val explanation = baseMatch?.copy(
                brandName = genericOrBrand,
                primaryPurposeBn = if (aiExplanation.isNotBlank()) aiExplanation else baseMatch.primaryPurposeBn,
                isFromCache = false
            ) ?: MedicineExplanation(
                genericName = genericOrBrand.replaceFirstChar { it.uppercase() },
                brandName = genericOrBrand,
                banglaName = genericOrBrand,
                therapeuticClass = "চিকিৎসক নির্দেশিত প্রেসক্রিপশন মেডিসিন",
                primaryPurposeBn = if (aiExplanation.isNotBlank()) aiExplanation else "রোগের উপশম ও শারীরিক সুস্থতার জন্য বিশেষজ্ঞ চিকিৎসকের পরামর্শে নির্দেশিত হয়েছে।",
                standardDosageBn = "প্রেসক্রিপশনে উল্লেখিত মাত্রা ও সময় অনুযায়ী সেবন করুন।",
                sideEffectsBn = "যেকোনো নতুন অস্বস্তি বা অ্যালার্জি দেখা দিলে চিকিৎসকের সাথে কথা বলুন।",
                criticalWarningsBn = "চিকিৎসকের পরামর্শ ছাড়া ওষুধের মাত্রা বাড়াবেন না বা বন্ধ করবেন না।",
                lifestylePrecautionsBn = "নিয়মিত পর্যাপ্ত পানি পান করুন ও সময়মত খাবার গ্রহণ করুন।",
                isFromCache = false
            )

            // Step 3: Persist in Room SQLite with 180-day TTL
            val entity = MedicineCacheEntity(
                generic_name_normalized = normalized,
                brand_aliases_json = "[\"$genericOrBrand\"]",
                bangla_name = explanation.banglaName,
                therapeutic_class = explanation.therapeuticClass,
                primary_purpose_bn = explanation.primaryPurposeBn,
                standard_dosage_bn = explanation.standardDosageBn,
                common_side_effects_bn = explanation.sideEffectsBn,
                critical_warnings_bn = explanation.criticalWarningsBn,
                lifestyle_precautions_bn = explanation.lifestylePrecautionsBn,
                cached_at = now,
                ttl_timestamp = now + ttlDurationMs,
                hit_count = 1
            )
            cacheDao.insertCache(entity)

            Resource.Success(explanation, isFromCache = false)
        }

    override suspend fun searchMedicines(query: String): List<String> = withContext(ioDispatcher) {
        val q = query.trim().lowercase()
        val fromDb = cacheDao.searchMedicines(q).map { it.bangla_name }
        val fromKb = localKnowledgeBase.keys.filter { it.contains(q) }
        (fromDb + fromKb).distinct()
    }

    override suspend fun saveMedicineExplanation(explanation: MedicineExplanation): Unit =
        withContext(ioDispatcher) {
            val now = System.currentTimeMillis()
            val entity = MedicineCacheEntity(
                generic_name_normalized = normalizeDrugName(explanation.genericName),
                brand_aliases_json = "[\"${explanation.brandName}\"]",
                bangla_name = explanation.banglaName,
                therapeutic_class = explanation.therapeuticClass,
                primary_purpose_bn = explanation.primaryPurposeBn,
                standard_dosage_bn = explanation.standardDosageBn,
                common_side_effects_bn = explanation.sideEffectsBn,
                critical_warnings_bn = explanation.criticalWarningsBn,
                lifestyle_precautions_bn = explanation.lifestylePrecautionsBn,
                cached_at = now,
                ttl_timestamp = now + ttlDurationMs,
                hit_count = 1
            )
            cacheDao.insertCache(entity)
        }

    private fun normalizeDrugName(name: String): String {
        return name.trim().lowercase()
            .replace("tab.", "")
            .replace("cap.", "")
            .replace("syr.", "")
            .replace(Regex("""\d+.*"""), "") // strip trailing strength like 500mg
            .trim()
    }
}
