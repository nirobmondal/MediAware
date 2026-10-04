package com.example.mediaware.features.symptom.domain.repository

import com.example.mediaware.features.symptom.domain.model.Symptom

object SymptomCatalog {
    val ALL_SYMPTOMS: List<Symptom> = listOf(
        Symptom("headache", "মাথাব্যথা", "মাথা ও মস্তিষ্ক", false, "head"),
        Symptom("severe_headache", "তীব্র মাথাব্যথা", "মাথা ও মস্তিষ্ক", true, "head_pain"),
        Symptom("blurred_vision", "চোখে ঝাপসা দেখা", "চোখ", true, "eye"),
        Symptom("slurred_speech", "কথা জড়িয়ে যাওয়া", "মুখ ও কথা", true, "record_voice_over"),
        Symptom("chest_pain", "বুকে তীব্র ব্যথা বা চাপ", "বুক ও হৃদযন্ত্র", true, "favorite"),
        Symptom("palpitations", "বুক ধড়ফড় করা", "বুক ও হৃদযন্ত্র", false, "monitor_heart"),
        Symptom("breathlessness", "শ্বাসকষ্ট", "ফুসফুস ও শ্বাসতন্ত্র", true, "air"),
        Symptom("cough", "কাশি ও কফ", "ফুসফুস ও শ্বাসতন্ত্র", false, "coronavirus"),
        Symptom("vomiting", "বমি ও বমিভাব", "পাকস্থলী ও পরিপাক", true, "sick"),
        Symptom("abdominal_pain", "পেটে তীব্র ব্যথা", "পাকস্থলী ও পরিপাক", false, "healing"),
        Symptom("high_fever", "উচ্চ জ্বর", "সার্বদৈহিক তাপমাত্রা", true, "thermostat"),
        Symptom("unconsciousness", "জ্ঞান হারানো বা অচেতন", "চেতনা ও স্নায়ুতন্ত্র", true, "psychology"),
        Symptom("one_sided_weakness", "হঠাৎ একপাশ অবশ বা প্যারালাইসিস", "স্নায়ুতন্ত্র ও পেশী", true, "accessible"),
        Symptom("extreme_fatigue", "তীব্র দুর্বলতা ও অবসাদ", "সার্বদৈহিক শক্তি", false, "battery_alert"),
        Symptom("leg_swelling", "পা ফোলা বা পানি আসা", "হাত ও পা", false, "accessibility"),
        Symptom("anemia", "রক্তশূন্যতা বা ফ্যাকাশে ভাব", "রক্ত সংবহন", false, "water_drop"),
        Symptom("back_pain", "কোমর ও পিঠে ব্যথা", "মেরুদণ্ড ও পিঠ", false, "fitness_center"),
        Symptom("urinary_burning", "প্রস্রাবে জ্বালাপোড়া বা রক্ত", "মূত্রতন্ত্র ও কিডনি", false, "water")
    )
}
