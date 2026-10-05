package com.example.mediaware.core.common.ai

import com.example.mediaware.core.common.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.HttpsURLConnection

@Singleton
class GeminiAiClient @Inject constructor() {

    private val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY

    /**
     * Explains a prescribed medicine in simple Bengali: purpose, administration tips, side effects, precautions.
     */
    suspend fun explainMedicine(
        name: String,
        genericName: String? = null,
        strength: String? = null,
        dosagePattern: String? = null,
        timing: String? = null
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            আপনি একজন অত্যন্ত সহমর্মী ও অভিজ্ঞ বাংলাদেশী ফার্মাসিস্ট ও স্বাস্থ্য পরামর্শক।
            নিচের ওষুধটির বিবরণ সহজ ও সাবলীল বাংলায় রোগীকে বুঝিয়ে বলুন:
            - ওষুধের নাম: $name
            - জেনেরিক নাম: ${genericName ?: "অজ্ঞাত"}
            - শক্তি/মাত্রা: ${strength ?: "প্রেসক্রিপশন অনুযায়ী"}
            - সেবনের নিয়ম: ${dosagePattern ?: "চিকিৎসকের পরামর্শ অনুযায়ী"} ($timing)

            অনুগ্রহ করে নিচে ৪টি সুস্পষ্ট প্যারাগ্রাফে উত্তর দিন:
            ১. এই ওষুধটি কী কাজে বা কোন উপসর্গের জন্য ডাক্তাররা দিয়ে থাকেন?
            ২. কীভাবে খেলে এটি সবচেয়ে ভালো কাজ করে (খাবারের আগে/পরে, পানি)?
            ৩. সম্ভাব্য সাধারণ পার্শ্বপ্রতিক্রিয়া (যেমন: হালকা মাথা ঘোরা, গ্যাস্ট্রিক ইত্যাদি)?
            ৪. বিশেষ সতর্কতা ও জরুরি পরামর্শ।

            (মনে রাখবেন: কোনো নতুন ডোজ নির্ধারণ করবেন না এবং শেষে চিকিৎসকের পরামর্শ নেওয়ার কথা মনে করিয়ে দিন)।
        """.trimIndent()

        val response = callGeminiApi(prompt)
        if (!response.isNullOrBlank()) {
            response
        } else {
            getMedicineFallbackExplanation(name, genericName, strength)
        }
    }

    /**
     * Explains a laboratory diagnostic test result in clear Bengali with lifestyle guidance.
     */
    suspend fun explainLabTest(
        testName: String,
        value: Double,
        unit: String,
        normalMin: Double,
        normalMax: Double,
        status: String
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            আপনি একজন অভিজ্ঞ বাংলাদেশী চিকিৎসক ও ল্যাবরেটরি কনসালট্যান্ট।
            রোগী তার ডায়াগনস্টিক ল্যাব রিপোর্ট নিয়ে চিন্তিত। সহজ ও শান্ত ভাষায় বাংলায় বুঝিয়ে বলুন:
            - ল্যাব পরীক্ষার নাম: $testName
            - প্রাপ্ত ফলাফল/মান: $value $unit
            - স্বাভাবিক পরিসীমা (Normal Range): $normalMin - $normalMax $unit
            - বর্তমান স্ট্যাটাস: $status

            অনুগ্রহ করে ৩টি পয়েন্টে বুঝিয়ে দিন:
            ১. এই ফলাফলটির সাধারণ অর্থ কী এবং শরীরের জন্য এটি কী ইঙ্গিত করছে?
            ২. খাদ্যাভ্যাস ও জীবনযাত্রার কোন কোন দিক খেয়াল রাখা ভালো?
            ৩. পরবর্তী সময়ে ডাক্তারের সাথে সাক্ষাতের সময় রোগীর কী কী বিষয় জানানো উচিত?

            (সতর্কতা: সরাসরি কোনো রোগ নির্ণয় করবেন না। এটি পরামর্শমূলক ও শিক্ষণীয় স্বাস্থ্য সহায়িকা)।
        """.trimIndent()

        val response = callGeminiApi(prompt)
        if (!response.isNullOrBlank()) {
            response
        } else {
            getLabTestFallbackExplanation(testName, value, unit, status)
        }
    }

    /**
     * Overall analysis of a prescription regimen and instructions.
     */
    suspend fun explainPrescription(
        medicinesSummary: String,
        rawOcrText: String? = null
    ): String = withContext(Dispatchers.IO) {
        val prompt = buildString {
            appendLine("আপনি একজন বিশেষজ্ঞ চিকিৎসক ও ফার্মাসিস্ট। নিচের প্রেসক্রিপশনটির একটি সমন্বিত ও সহজবোধ্য বাংলা বিশ্লেষণ প্রদান করুন:")
            if (!rawOcrText.isNullOrBlank()) {
                appendLine("প্রেসক্রিপশনের ওসিআর টেক্সট:")
                appendLine("\"\"\"$rawOcrText\"\"\"")
            }
            appendLine("প্রেসক্রিপশনভুক্ত ওষুধসমূহ:")
            appendLine(medicinesSummary)
            appendLine()
            appendLine("অনুগ্রহ করে নিচের বিষয়গুলো স্পষ্ট ভাষায় রোগীকে বুঝিয়ে বলুন:")
            appendLine("১. প্রেসক্রিপশনের সামগ্রিক উদ্দেশ্য ও রোগ নিয়ন্ত্রণের পরিকল্পনা।")
            appendLine("২. ওষুধগুলো একসাথে কীভাবে কাজ করে এবং খাওয়ার নিয়মকানুন।")
            appendLine("৩. বিশেষ সতর্কতা, পথ্য ও জীবনযাত্রার নির্দেশিকা।")
            appendLine("৪. কতদিন পর বা কী ধরনের লক্ষণ দেখলে পুনরায় ডাক্তারের কাছে যাওয়া উচিত।")
            appendLine("(সতর্কতা: কোনো নতুন ডোজ নির্ধারণ করবেন না; এটি সহায়ক শিক্ষণীয় তথ্য)।")
        }

        val response = callGeminiApi(prompt)
        if (!response.isNullOrBlank()) {
            response
        } else {
            getPrescriptionFallbackSummary(medicinesSummary)
        }
    }

    /**
     * Overall analysis of a diagnostic laboratory test report.
     */
    suspend fun explainLabReportOverall(testsSummary: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            আপনি একজন অভিজ্ঞ বাংলাদেশী প্যাথলজিস্ট ও পরামর্শক চিকিৎসক।
            রোগী তার ডায়াগনস্টিক ল্যাব টেস্টগুলোর রিপোর্ট পেয়েছে। নিচের টেস্ট ও ফলাফলগুলোর একটি সামগ্রিক, সহমর্মী ও সহজবোধ্য বাংলা বিশ্লেষণ দিন:

            $testsSummary

            অনুগ্রহ করে ৩টি পয়েন্টে স্পষ্ট করে লিখুন:
            ১. সামগ্রিক মূল্যায়ন: এই টেস্টগুলোর ফলাফল শরীরের কোন কোন অঙ্গ বা সিস্টেম (যেমন: লিভার, কিডনি, রক্তে শর্করা, রক্তস্বল্পতা) সম্পর্কে কী ইঙ্গিত দিচ্ছে?
            ২. খাদ্যাভ্যাস ও জীবনযাত্রা: ফলাফলগুলো স্বাভাবিক সীমার মধ্যে রাখতে কী কী খাদ্য গ্রহণ বা বর্জন করা উচিত?
            ৩. পরবর্তী পরামর্শ: ডাক্তারের সাথে সাক্ষাতের সময় কোন বিষয়গুলো আলোচনা করা গুরুত্বপূর্ণ?

            (সতর্কতা: সরাসরি কোনো নির্দিষ্ট রোগ চূড়ান্ত ডায়াগনসিস করবেন না; ডাক্তারের পরামর্শ নেওয়ার তাগিদ দিন)।
        """.trimIndent()

        val response = callGeminiApi(prompt)
        if (!response.isNullOrBlank()) {
            response
        } else {
            "আপনার ল্যাব টেস্টের রিপোর্ট অনুযায়ী কিছু মান স্বাভাবিক সীমার কাছাকাছি বা বাইরে থাকতে পারে। শরীরের সামগ্রিক স্বাস্থ্য সুরক্ষায় নিয়মিত পুষ্টিকর খাদ্য গ্রহণ করুন, পর্যাপ্ত পানি পান করুন এবং চূড়ান্ত নির্দেশনার জন্য চিকিৎসকের সাথে পরামর্শ করুন।"
        }
    }

    /**
     * Analyzes disease/symptoms and dynamically generates targeted cheat questions for doctor consultation.
     */
    suspend fun analyzeDiseaseAndSymptoms(
        symptoms: List<String>,
        severity: Int,
        duration: String,
        chronicConditions: List<String>,
        age: Int? = null,
        gender: String? = null
    ): Pair<String, List<String>> = withContext(Dispatchers.IO) {
        val symptomsStr = if (symptoms.isNotEmpty()) symptoms.joinToString(", ") else "শারীরিক অস্বস্তি"
        val conditionsStr = if (chronicConditions.isNotEmpty()) chronicConditions.joinToString(", ") else "নেই"
        val demog = "বয়স: ${age ?: 40} বছর, লিঙ্গ: ${gender ?: "উল্লেখ নেই"}"

        val prompt = """
            রোগীর স্বাস্থ্য তথ্য:
            - রোগীর প্রোফাইল: $demog
            - অনুভূত লক্ষণসমূহ: $symptomsStr
            - কষ্টের তীব্রতা (১ থেকে ১০ স্কেলে): $severity
            - স্থায়ীত্বকাল: $duration
            - পূর্বের দীর্ঘস্থায়ী রোগ: $conditionsStr

            একজন বিশেষজ্ঞ চিকিৎসক হিসেবে রোগীকে প্রস্তুত করতে সাহায্য করুন।
            JSON ফরম্যাটে রেসপন্স দিন:
            {
               "analysis": "লক্ষণগুলোর একটি পরিষ্কার ও সহমর্মী বিশ্লেষণ (বাংলায় ২-৩ প্যারাগ্রাফে)। কেন এমন হতে পারে এবং কী কী দিকে নজর রাখা প্রয়োজন।",
               "cheat_questions": [
                   "ডাক্তারকে জিজ্ঞেস করার জন্য প্রথম প্রধান প্রশ্ন",
                   "দ্বিতীয় প্রশ্ন",
                   "তৃতীয় প্রশ্ন",
                   "চতুর্থ প্রশ্ন",
                   "পঞ্চম প্রশ্ন"
               ]
            }
            শুধুমাত্র বৈধ JSON প্রদান করুন।
        """.trimIndent()

        val jsonResponse = callGeminiApi(prompt)
        if (!jsonResponse.isNullOrBlank()) {
            try {
                val cleanedJson = jsonResponse.replace("```json", "").replace("```", "").trim()
                val jsonObj = JSONObject(cleanedJson)
                val analysis = jsonObj.optString("analysis", "")
                val questionsArray = jsonObj.optJSONArray("cheat_questions")
                val questionsList = mutableListOf<String>()
                if (questionsArray != null) {
                    for (i in 0 until questionsArray.length()) {
                        questionsList.add(questionsArray.getString(i))
                    }
                }
                if (analysis.isNotBlank() && questionsList.isNotEmpty()) {
                    return@withContext Pair(analysis, questionsList)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error parsing Gemini JSON for disease/symptoms analysis")
            }
        }

        // Reliable fallback
        generateFallbackCheatQuestions(symptoms, severity)
    }

    /**
     * Backward-compatible alias for analyzeDiseaseAndSymptoms
     */
    suspend fun analyzeSymptomsAndGenerateCheatQuestions(
        symptoms: List<String>,
        severity: Int,
        duration: String,
        chronicConditions: List<String>
    ): Pair<String, List<String>> = analyzeDiseaseAndSymptoms(
        symptoms = symptoms,
        severity = severity,
        duration = duration,
        chronicConditions = chronicConditions
    )

    private fun callGeminiApi(promptText: String): String? {
        if (apiKey.isBlank()) {
            Timber.w("Gemini API key is blank; using fallback response")
            return null
        }

        // Use modern supported Gemini models with fallback
        val models = listOf("gemini-3.5-flash-lite", "gemini-3.5-flash")

        for (model in models) {
            var connection: HttpsURLConnection? = null
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                connection = (url.openConnection() as HttpsURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connectTimeout = 12000
                    readTimeout = 12000
                    doOutput = true
                }

                // Construct JSON request body
                val requestBody = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply { put("text", promptText) })
                            }
                            put("parts", parts)
                        }
                        put(contentObj)
                    }
                    put("contents", contents)
                }

                OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
                    writer.write(requestBody.toString())
                    writer.flush()
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    val responseText = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8")).use { reader ->
                        reader.readText()
                    }

                    val responseJson = JSONObject(responseText)
                    val candidates = responseJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val candidate = candidates.getJSONObject(0)
                        val content = candidate.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val text = parts.getJSONObject(0).optString("text")
                            if (text.isNotBlank()) {
                                return text
                            }
                        }
                    }
                } else {
                    Timber.w("Gemini model $model returned HTTP $responseCode, trying next model...")
                }
            } catch (e: Exception) {
                Timber.w("Gemini model $model call failed: ${e.message}")
            } finally {
                connection?.disconnect()
            }
        }

        return null
    }

    private fun getMedicineFallbackExplanation(name: String, genericName: String?, strength: String?): String {
        return """
            $name ${strength ?: ""} (${genericName ?: "প্রেসক্রিপশন অনুযায়ী"}) একটি অত্যন্ত প্রয়োজনীয় ওষুধ।
            • এটি নিয়মিত সময়ে নির্দেশিত নিয়মে পানি দিয়ে সেবন করা উচিত।
            • খালি পেটে বা খাবারের পর চিকিৎসকের নির্দেশ মোতাবেক গ্রহণ করুন।
            • সেবনকালীন পর্যাপ্ত পানি পান করুন এবং অপ্রত্যাশিত প্রতিক্রিয়া দেখা দিলে ডাক্তারকে অবহিত করুন।
        """.trimIndent()
    }

    private fun getLabTestFallbackExplanation(testName: String, value: Double, unit: String, status: String): String {
        return """
            $testName টেস্টে আপনার ফলাফল এসেছে $value $unit ($status)।
            • ফলাফল স্বাভাবিক মাত্রার সাথে তুলনা করে চিকিৎসকের সাথে পরবর্তী চিকিৎসা পরিকল্পনা নিয়ে আলোচনা করুন।
            • নিয়মিত হালকা ব্যায়াম, পরিমিত পানি ও পুষ্টিকর খাবার স্বাস্থ্য বজায় রাখতে সাহায্য করে।
        """.trimIndent()
    }

    private fun getPrescriptionFallbackSummary(medicinesSummary: String): String {
        return """
            প্রেসক্রিপশনে নির্দেশিত ওষুধগুলো আপনার শারীরিক সুস্থতা ও উপসর্গ উপশমের জন্য নির্ধারিত হয়েছে।
            • প্রতিটি ওষুধ চিকিৎসকের দেওয়া সময়সূচি ও খাবারের নিয়ম (খাবারের আগে বা পরে) কঠোরভাবে মেনে সেবন করুন।
            • কোনো ওষুধ চিকিৎসকের পরামর্শ ছাড়া বাদ দেওয়া বা অতিরিক্ত সেবন করা থেকে বিরত থাকুন।
            • প্রেসক্রিপশনের কোর্স শেষ হলে বা শারীরিক কোনো অস্বস্তি দেখা দিলে অবিলম্বে চিকিৎসকের সাথে যোগাযোগ করুন।
        """.trimIndent()
    }

    private fun generateFallbackCheatQuestions(symptoms: List<String>, severity: Int): Pair<String, List<String>> {
        val analysis = "আপনার অনুভূত লক্ষণসমূহের তীব্রতা $severity/১০। লক্ষণগুলো দীর্ঘস্থায়ী হলে বা তীব্রতা বাড়লে চিকিৎসকের সরাসরি মূল্যায়ন অত্যন্ত জরুরি।"
        val questions = mutableListOf<String>()

        if (symptoms.any { it.contains("বুক") || it.contains("শ্বাস") }) {
            questions.add("আমার এই বুকের চাপ বা অস্বস্তি কি হৃদযন্ত্র বা গ্যাস্ট্রিকের সাথে সম্পর্কিত?")
            questions.add("আমার কি কোনো ইসিজি (ECG) বা বুকের এক্স-রে করানো প্রয়োজন?")
        }
        if (symptoms.any { it.contains("মাথা") || it.contains("ঘোরা") }) {
            questions.add("এই মাথা ঘোরার সাথে আমার রক্তচাপ বা চোখের সমস্যার কোনো সম্পর্ক আছে কি?")
            questions.add("হঠাৎ মাথা ঘুরলে তাৎক্ষণিকভাবে কী প্রাথমিক পদক্ষেপ নেওয়া উচিত?")
        }
        if (symptoms.any { it.contains("পেট") || it.contains("বমি") || it.contains("বদহজম") }) {
            questions.add("পেটের এই সমস্যার জন্য কি কোনো নির্দিষ্ট খাবার সাময়িকভাবে বর্জন করতে হবে?")
            questions.add("আল্ট্রাসনোগ্রাম বা অন্য কোনো পরীক্ষা করার পরামর্শ দেবেন কি?")
        }
        if (symptoms.any { it.contains("জ্বর") || it.contains("কাশি") }) {
            questions.add("জ্বরের স্থায়ীত্বের ওপর ভিত্তি করে কি সিবিসি বা ডেঙ্গু এনএস১ পরীক্ষা করা লাগবে?")
            questions.add("প্যারাসিটামলের পাশাপাশি কি কোনো অ্যান্টিবায়োটিক দরকার আছে?")
        }

        // Add general core questions
        questions.add("আমার বর্তমান লক্ষণগুলো পুরোপুরি ভালো হতে আনুমানিক কতদিন সময় লাগতে পারে?")
        questions.add("লক্ষণ কতটুকু বাড়লে আমার জরুরি বিভাগে অবিলম্বে যোগাযোগ করা উচিত?")

        return Pair(analysis, questions.distinct())
    }
}
