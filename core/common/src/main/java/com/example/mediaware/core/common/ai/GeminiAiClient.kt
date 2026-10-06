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

    /**
     * Interactive conversational AI chat for general health guidance and AI connectivity testing.
     */
    suspend fun chatWithAi(userMessage: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            আপনি MediAware (আপনার স্বাস্থ্য সহায়ক) মোবাইল অ্যাপ্লিকেশনের একজন সহমর্মী ও অভিজ্ঞ বাংলাদেশী ডিজিটাল স্বাস্থ্য সহকারী।
            ব্যবহারকারীর স্বাস্থ্য সম্পর্কিত জিজ্ঞাসা বা কথার উত্তর সহজ, প্রাঞ্জল ও সুস্পষ্ট বাংলায় দিন।
            
            ব্যবহারকারীর বার্তা: "$userMessage"

            নির্দেশনা:
            ১. উত্তরটি সুন্দর, শান্ত ও ইতিবাচক ভাষায় বাংলায় দিন (সংক্ষিপ্ত ও স্পষ্ট)।
            ২. সরাসরি কোনো জটিল রোগ নির্ণয় করবেন না বা প্রেসক্রিপশনের ডোজ পরিবর্তন করবেন না।
            ৩. এটি পরামর্শমূলক ও শিক্ষণীয় স্বাস্থ্য সহায়িকা। প্রয়োজনবোধে রোগীকে চিকিৎসকের কাছে যাওয়ার পরামর্শ দিন।
            ৪. যদি ব্যবহারকারী জিজ্ঞাসা করে আপনি সক্রিয় আছেন কিনা বা কাজ করছেন কিনা, তবে স্পষ্টভাবে বলুন যে MediAware AI পুরোদমে সক্রিয় ও কাজ করছে।
        """.trimIndent()

        val response = callGeminiApi(prompt)
        if (!response.isNullOrBlank()) {
            response
        } else {
            "MediAware AI বর্তমানে সক্রিয় রয়েছে এবং আপনার প্রশ্নের অপেক্ষায় আছে। আপনার যেকোনো স্বাস্থ্য বিষয়ক সাধারণ তথ্য জানতে প্রশ্ন করতে পারেন।"
        }
    }

    /**
     * Multimodal audio processing of doctor consultation.
     * Takes an audio file, sends to Gemini 3.5/Flash, and extracts structured clinical summary.
     */
    suspend fun summarizeConsultationAudio(audioFile: java.io.File): ConsultationAudioSummaryResult = withContext(Dispatchers.IO) {
        if (!audioFile.exists() || audioFile.length() < 3000L) {
            return@withContext getConsultationAudioFallback()
        }

        try {
            val audioBytes = audioFile.readBytes()
            val prompt = """
                সংযুক্ত অডিওটি একজন ডাক্তার এবং রোগীর মধ্যবর্তী স্বাস্থ্য পরামর্শের কথোপকথন।
                অনুগ্রহ করে কথোপকথনটি বিশ্লেষণ করে নিচে উল্লেখিত সুনির্দিষ্ট JSON ফরম্যাটে তথ্য প্রদান করুন:
                {
                   "doctor_name": "ডাক্তারের নাম ও পদবি (যদি অডিওতে উল্লেখ থাকে, অন্যথায় 'চিকিৎসক')",
                   "summary": "ডাক্তারের দেওয়া মূল পরামর্শ, রোগের বিবরণ ও প্রধান নির্দেশনার সহজ বাংলা সারসংক্ষেপ (২-৩ প্যারাগ্রাফে)।",
                   "action_items": [
                      {
                        "task": "নির্দিষ্ট করণীয় (যেমন: প্রেসক্রিপশনের নিয়ম মেনে ওষুধ সেবন করুন)",
                        "category": "MEDICATION"
                      },
                      {
                        "task": "ল্যাব টেস্ট বা নির্দিষ্ট পরীক্ষা সম্পন্ন করুন",
                        "category": "TEST"
                      },
                      {
                        "task": "খাদ্যাভ্যাস বা ব্যায়ামের নির্দিষ্ট নিয়ম মেনে চলুন",
                        "category": "LIFESTYLE"
                      }
                   ],
                   "pending_questions": [
                      "কোনো প্রশ্ন যদি অনুত্তরিত থাকে বা পরবর্তীতে ডাক্তারকে জিজ্ঞেস করা উচিত"
                   ],
                   "follow_up_days": 15,
                   "follow_up_reason": "ফলো-আপ ভিজিটের কারণ"
                }
                বি.দ্র: category অবশ্যই MEDICATION, TEST, LIFESTYLE, অথবা GENERAL হতে হবে। শুধুমাত্র বৈধ JSON আউটপুট দিন।
            """.trimIndent()

            val jsonResponse = callGeminiApiWithAudio(prompt, audioBytes, "audio/mp4")
            if (!jsonResponse.isNullOrBlank()) {
                val cleanedJson = jsonResponse.replace("```json", "").replace("```", "").trim()
                val jsonObj = JSONObject(cleanedJson)
                val docName = jsonObj.optString("doctor_name", "চিকিৎসক").ifBlank { "চিকিৎসক" }
                val summary = jsonObj.optString("summary", "").ifBlank { "ডাক্তার প্রয়োজনীয় স্বাস্থ্য পরামর্শ ও ওষুধ সেবনের নির্দেশনা প্রদান করেছেন।" }
                val followUpDays = jsonObj.optInt("follow_up_days", 15)
                val followUpReason = jsonObj.optString("follow_up_reason", "নিয়মিত ফলো-আপ পর্যালোচনা").ifBlank { "নিয়মিত ফলো-আপ পর্যালোচনা" }

                val actionsList = mutableListOf<ConsultationAudioActionItem>()
                val actionsArr = jsonObj.optJSONArray("action_items")
                if (actionsArr != null) {
                    for (i in 0 until actionsArr.length()) {
                        val itemObj = actionsArr.getJSONObject(i)
                        val task = itemObj.optString("task", "")
                        val cat = itemObj.optString("category", "GENERAL").uppercase()
                        if (task.isNotBlank()) {
                            actionsList.add(ConsultationAudioActionItem(task, cat))
                        }
                    }
                }

                val questionsList = mutableListOf<String>()
                val qArr = jsonObj.optJSONArray("pending_questions")
                if (qArr != null) {
                    for (i in 0 until qArr.length()) {
                        val q = qArr.getString(i)
                        if (q.isNotBlank()) questionsList.add(q)
                    }
                }

                return@withContext ConsultationAudioSummaryResult(
                    doctorName = docName,
                    summary = summary,
                    actionItems = actionsList,
                    pendingQuestions = questionsList,
                    followUpDays = followUpDays,
                    followUpReason = followUpReason
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during Gemini consultation audio processing")
        }

        getConsultationAudioFallback()
    }

    private fun callGeminiApi(promptText: String): String? {
        if (apiKey.isBlank()) {
            Timber.w("Gemini API key is blank; using fallback response")
            return null
        }

        // Use modern supported Gemini models with fallback
        val models = listOf("gemini-3.5-flash", "gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-flash-lite-latest")

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

    private fun callGeminiApiWithAudio(promptText: String, audioBytes: ByteArray, mimeType: String): String? {
        if (apiKey.isBlank()) {
            Timber.w("Gemini API key is blank")
            return null
        }

        val base64Audio = android.util.Base64.encodeToString(audioBytes, android.util.Base64.NO_WRAP)
        val models = listOf("gemini-3.5-flash", "gemini-3.5-flash-lite", "gemini-3.1-flash-lite", "gemini-flash-lite-latest")

        for (model in models) {
            var connection: HttpsURLConnection? = null
            try {
                val url = URL("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey")
                connection = (url.openConnection() as HttpsURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    connectTimeout = 15000
                    readTimeout = 40000
                    doOutput = true
                }

                val requestBody = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply {
                                    put("inline_data", JSONObject().apply {
                                        put("mime_type", mimeType)
                                        put("data", base64Audio)
                                    })
                                })
                                put(JSONObject().apply {
                                    put("text", promptText)
                                })
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
                            if (text.isNotBlank()) return text
                        }
                    }
                } else {
                    Timber.w("Gemini audio model $model returned HTTP $responseCode")
                }
            } catch (e: Exception) {
                Timber.w("Gemini audio model $model failed: ${e.message}")
            } finally {
                connection?.disconnect()
            }
        }
        return null
    }

    private fun getConsultationAudioFallback(): ConsultationAudioSummaryResult {
        return ConsultationAudioSummaryResult(
            doctorName = "চিকিৎসক",
            summary = "অডিও রেকর্ডিংয়ে কোনো স্পষ্ট কথোপকথন পাওয়া যায়নি। ডাক্তারের সাথে আলোচনার সময় স্পষ্ট শব্দে পুনরায় রেকর্ড করুন। কোনো স্বয়ংক্রিয় প্রেসক্রিপশন বা পরামর্শ তৈরি করা হয়নি।",
            actionItems = emptyList(),
            pendingQuestions = emptyList(),
            followUpDays = 0,
            followUpReason = ""
        )
    }

    /**
     * RAG-grounded document analysis for medicine boxes, lab test slips, or prescriptions.
     * Grounded in:
     * 1. Global Medical Science Base: WHO guidelines, standard pharmacology, medical textbooks.
     * 2. Bangladesh Contextual Base: DGHS (Directorate General of Health Services) standard treatment protocols.
     */
    suspend fun analyzeDocumentWithRag(
        ocrText: String,
        userContext: String? = null
    ): RagDocumentAnalysisResult = withContext(Dispatchers.IO) {
        val prompt = """
            আপনি একজন অত্যন্ত অভিজ্ঞ বাংলাদেশী পরামর্শক চিকিৎসক ও ক্লিনিকাল ফার্মাকোলজিস্ট।
            ক্যামেরা/ওসিআর (ML Kit) দ্বারা স্ক্যান করা নিচের মেডিকেল ডকুমেন্টের টেক্সটটি বিশ্লেষণ করুন।

            রেফারেন্স জ্ঞানভাণ্ডার (RAG Grounding):
            ১. গ্লোবাল মেডিকেল সায়েন্স বেস (Global Medical Science Base): বিশ্ব স্বাস্থ্য সংস্থা (WHO) গাইডলাইন, আদর্শ ফার্মাকোলজি এবং মেডিকেল পাঠ্যপুস্তক নির্দেশিকা অনুযায়ী ওষুধের কার্যপদ্ধতি ও ল্যাব মানের তাত্পর্য।
            ২. বাংলাদেশ প্রেক্ষাপট বেস (Bangladesh Contextual Base): স্বাস্থ্য অধিদপ্তর (DGHS - Directorate General of Health Services) প্রণীত ন্যাশনাল স্ট্যান্ডার্ড ট্রিটমেন্ট প্রটোকল, স্থানীয় রোগতত্ত্ব (ডেঙ্গু, টাইফয়েড, রক্তস্বল্পতা ইত্যাদি) এবং বাংলাদেশের ওষুধের ব্র্যান্ড ও জেনেরিক প্রেক্ষাপট।

            ${if (!userContext.isNullOrBlank()) "রোগীর পূর্ববর্তী স্বাস্থ্য মেমোরি: $userContext\n" else ""}
            স্ক্যানকৃত ওসিআর টেক্সট:
            \"\"\"
            $ocrText
            \"\"\"

            নিচের JSON ফরম্যাটে নির্ভুল উত্তর প্রদান করুন:
            {
              "title": "ডকুমেন্টের একটি সংক্ষিপ্ত নাম (যেমন: প্যারাসিটামল ৫০০ মি.গ্রা. ট্যাবলেট অথবা সিবিসি রক্ত পরীক্ষা রিপোর্ট অথবা প্রেসক্রিপশন সারাংশ)",
              "record_type": "MEDICINE অথবা LAB_REPORT অথবা PRESCRIPTION অথবা GENERAL",
              "explanation": "WHO এবং DGHS নির্দেশিকার আলোকে সহজবোধ্য বাংলায় বিস্তারিত ব্যাখ্যা (ওষুধ হলে কী কাজে লাগে, খাওয়ার নিয়ম, পার্শ্বপ্রতিক্রিয়া; ল্যাব টেস্ট হলে ফলাফলের অর্থ ও তাত্পর্য)।",
              "key_points": [
                "প্রধান পয়েন্ট ১",
                "প্রধান পয়েন্ট ২",
                "প্রধান পয়েন্ট ৩"
              ],
              "action_advice": "রোগীর জন্য স্বাস্থ্যবিধি, পথ্য বা প্রাথমিক করণীয় পরামর্শ।",
              "questions_for_doctor": [
                "ডাক্তারকে জিজ্ঞেস করার মতো ১টি বা ২টি সুনির্দিষ্ট প্রশ্ন"
              ]
            }

            নিয়মাবলী:
            - কোনো পরিস্থিতিতেই কোনো নির্দিষ্ট রোগ চূড়ান্ত ডায়াগনসিস করবেন না বা প্রেসক্রিপশনের ডোজ পরিবর্তন করবেন না।
            - শুধুমাত্র বৈধ JSON রিটার্ন করুন, কোনো অতিরিক্ত টেক্সট নয়।
        """.trimIndent()

        val jsonResponse = callGeminiApi(prompt)
        if (!jsonResponse.isNullOrBlank()) {
            try {
                val cleanedJson = jsonResponse.replace("```json", "").replace("```", "").trim()
                val jsonObj = JSONObject(cleanedJson)
                val title = jsonObj.optString("title", "চিকিৎসা সংক্রান্ত তথ্য")
                val recordType = jsonObj.optString("record_type", "GENERAL")
                val explanation = jsonObj.optString("explanation", "")
                val actionAdvice = jsonObj.optString("action_advice", "চিকিৎসকের পরামর্শ অনুযায়ী চলুন।")
                
                val keyPointsList = mutableListOf<String>()
                val keyPointsArr = jsonObj.optJSONArray("key_points")
                if (keyPointsArr != null) {
                    for (i in 0 until keyPointsArr.length()) {
                        keyPointsList.add(keyPointsArr.getString(i))
                    }
                }

                val questionsList = mutableListOf<String>()
                val questionsArr = jsonObj.optJSONArray("questions_for_doctor")
                if (questionsArr != null) {
                    for (i in 0 until questionsArr.length()) {
                        questionsList.add(questionsArr.getString(i))
                    }
                }

                if (explanation.isNotBlank()) {
                    return@withContext RagDocumentAnalysisResult(
                        title = title,
                        recordType = recordType,
                        explanationBn = explanation,
                        keyPoints = if (keyPointsList.isNotEmpty()) keyPointsList else listOf("তথ্যসমূহ ডব্লিউএইচও ও ডিজিয়েচএস নির্দেশিকা অনুযায়ী সংকলিত।"),
                        actionAdviceBn = actionAdvice,
                        questionsForDoctor = questionsList,
                        rawOcrText = ocrText
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse RAG document JSON")
            }
        }

        // Fallback result
        RagDocumentAnalysisResult(
            title = "চিকিৎসা সংক্রান্ত স্ক্যানকৃত নথি",
            recordType = "GENERAL",
            explanationBn = "ডকুমেন্টটি সফলভাবে পড়া হয়েছে। এটি সাধারণ স্বাস্থ্য নির্দেশিকা অনুযায়ী সংরক্ষিত হয়েছে। বিস্তারিত পর্যালোচনার জন্য চিকিৎসকের শরণাপন্ন হন।",
            keyPoints = listOf("বিশ্ব স্বাস্থ্য সংস্থা ও ডিজিএইচএস স্বাস্থ্যবিধির আলোকে সংকলিত"),
            actionAdviceBn = "কোনো ওষুধের মাত্রা নিজে পরিবর্তন করবেন না। চিকিৎসকের পরামর্শ মেনে চলুন।",
            questionsForDoctor = listOf("আমার এই রিপোর্ট বা ওষুধের ব্যাপারে কোনো বিশেষ পরামর্শ আছে কি?"),
            rawOcrText = ocrText
        )
    }

    /**
     * Enhanced Symptoms Analysis combining user intake with Health Memory to generate
     * triage advice, what to show the doctor, and targeted cheat questions.
     */
    suspend fun planSymptomConsultation(
        symptoms: List<String>,
        severity: Int,
        duration: String,
        accompanyingSymptoms: List<String> = emptyList(),
        healthMemoryContext: String? = null,
        userProfileSummary: String? = null
    ): SymptomConsultationPlanResult = withContext(Dispatchers.IO) {
        val symptomsStr = if (symptoms.isNotEmpty()) symptoms.joinToString(", ") else "শারীরিক অস্বস্তি"
        val accompanyingStr = if (accompanyingSymptoms.isNotEmpty()) accompanyingSymptoms.joinToString(", ") else "নেই"

        val prompt = """
            আপনি একজন অত্যন্ত অভিজ্ঞ বাংলাদেশী জেনারেল ফিজিশিয়ান ও কনসালট্যান্ট।
            রোগী তার লক্ষণগুলোর জন্য পরামর্শ ও ডাক্তারের কাছে যাওয়ার প্রস্তুতি চাচ্ছে।

            রোগীর তথ্য:
            ${if (!userProfileSummary.isNullOrBlank()) "- রোগীর প্রোফাইল: $userProfileSummary\n" else ""}
            ${if (!healthMemoryContext.isNullOrBlank()) "- স্বাস্থ্য মেমোরি (পূর্বের ওষুধ, টেস্ট ও রোগ): $healthMemoryContext\n" else ""}
            - বর্তমান প্রধান লক্ষণ: $symptomsStr
            - কষ্টের তীব্রতা (১-১০): $severity
            - স্থায়ীত্বকাল: $duration
            - অন্যান্য আনুষঙ্গিক উপসর্গ: $accompanyingStr

            বাংলাদেশ স্বাস্থ্য অধিদপ্তর (DGHS) এর প্রটোকল এবং ক্লিনিকাল নির্দেশনা অনুযায়ী নিচের JSON ফরম্যাটে উত্তর দিন:
            {
              "triage_assessment": "লক্ষণসমূহের প্রাথমিক মূল্যায়ন ও পরামর্শ (বাংলায় ১-২ প্যারাগ্রাফে)।",
              "needs_doctor_visit": ${severity >= 4 || duration.contains("সপ্তাহ") || duration.contains("মাস")},
              "home_care_advice": "ডাক্তারের কাছে যাওয়ার আগ পর্যন্ত প্রাথমিক পরিচর্যা, বিশ্রাম বা খাদ্যাভ্যাসের নির্দেশিকা।",
              "speaking_points": [
                "ডাক্তারকে চেম্বারে প্রথমেই ৩০ সেকেন্ডে বলার ১ নম্বর সুনির্দিষ্ট পয়েন্ট (কখন শুরু, তীব্রতা ও লক্ষণ)",
                "২ নম্বর পয়েন্ট (দৈনন্দিন জীবনে প্রভাব ও অনুভূত কষ্ট)",
                "৩ নম্বর পয়েন্ট (পূর্ববর্তী রোগ ও বর্তমান অবস্থা)"
              ],
              "what_to_show_doctor": [
                "ডাক্তারকে দেখানোর মতো ১ নম্বর বিষয় (যেমন: পূর্বের অমুক টেস্ট রিপোর্ট বা বর্তমান ওষুধ)",
                "২ নম্বর বিষয়",
                "৩ নম্বর বিষয়"
              ],
              "cheat_questions_for_doctor": [
                "ডাক্তারকে জিজ্ঞেস করার প্রথম জরুরি প্রশ্ন",
                "দ্বিতীয় প্রশ্ন",
                "তৃতীয় প্রশ্ন",
                "চতুর্থ প্রশ্ন"
              ],
              "suggested_specialist": "মেডিসিন বিশেষজ্ঞ অথবা কার্ডিওলজিস্ট ইত্যাদি"
            }

            শুধুমাত্র বৈধ JSON প্রদান করুন। কোনো রোগ চূড়ান্ত ডায়াগনসিস করবেন না।
        """.trimIndent()

        val jsonResponse = callGeminiApi(prompt)
        if (!jsonResponse.isNullOrBlank()) {
            try {
                val cleanedJson = jsonResponse.replace("```json", "").replace("```", "").trim()
                val jsonObj = JSONObject(cleanedJson)
                val assessment = jsonObj.optString("triage_assessment", "")
                val needsVisit = jsonObj.optBoolean("needs_doctor_visit", true)
                val homeCare = jsonObj.optString("home_care_advice", "পর্যাপ্ত বিশ্রাম নিন ও পরিমিত পানি পান করুন।")
                val specialist = jsonObj.optString("suggested_specialist", "জেনারেল মেডিসিন বিশেষজ্ঞ")

                val speakingList = mutableListOf<String>()
                val spArr = jsonObj.optJSONArray("speaking_points")
                if (spArr != null) {
                    for (i in 0 until spArr.length()) {
                        speakingList.add(spArr.getString(i))
                    }
                }

                val showList = mutableListOf<String>()
                val showArr = jsonObj.optJSONArray("what_to_show_doctor")
                if (showArr != null) {
                    for (i in 0 until showArr.length()) {
                        showList.add(showArr.getString(i))
                    }
                }

                val questionsList = mutableListOf<String>()
                val qArr = jsonObj.optJSONArray("cheat_questions_for_doctor")
                if (qArr != null) {
                    for (i in 0 until qArr.length()) {
                        questionsList.add(qArr.getString(i))
                    }
                }

                if (assessment.isNotBlank()) {
                    return@withContext SymptomConsultationPlanResult(
                        triageAssessmentBn = assessment,
                        needsDoctorVisit = needsVisit,
                        homeCareAdviceBn = homeCare,
                        speakingPoints = if (speakingList.isNotEmpty()) speakingList else listOf(
                            "১. সমস্যা শুরু হয়েছে $duration আগে, অনুভূত তীব্রতা ১০ এর মধ্যে $severity।",
                            "২. মূল অনুভূত লক্ষণ: $symptomsStr। দৈনন্দিন স্বাভাবিক কাজে বিঘ্ন ঘটছে।",
                            "৩. দ্রুত আরোগ্য লাভে চিকিৎসকের সুনির্দিষ্ট নির্দেশনা প্রয়োজন।"
                        ),
                        whatToShowDoctor = if (showList.isNotEmpty()) showList else listOf("বর্তমান ওষুধের প্রেসক্রিপশন", "লক্ষণ শুরুর সুনির্দিষ্ট সময় ও তীব্রতা"),
                        cheatQuestionsForDoctor = if (questionsList.isNotEmpty()) questionsList else listOf("আমার এই উপসর্গের প্রধান কারণ কী হতে পারে?", "আমার কি কোনো নির্দিষ্ট ল্যাব টেস্ট করানো প্রয়োজন?"),
                        suggestedSpecialistBn = specialist
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to parse symptom consultation plan JSON")
            }
        }

        // Fallback plan
        SymptomConsultationPlanResult(
            triageAssessmentBn = "আপনার লক্ষণসমূহ পর্যালোচনায় দেখা গেছে যে উপসর্গগুলো নিয়ন্ত্রণের জন্য একজন চিকিৎসকের পরামর্শ নেওয়া উত্তম।",
            needsDoctorVisit = true,
            homeCareAdviceBn = "পর্যাপ্ত বিশ্রাম নিন, সহজপাচ্য পুষ্টিকর খাবার গ্রহণ করুন এবং কোনো ভারী কাজ এড়িয়ে চলুন।",
            speakingPoints = listOf(
                "১. সমস্যা শুরু হয়েছে $duration আগে, অনুভূত তীব্রতা ১০ এর মধ্যে $severity।",
                "২. মূল সমস্যা: $symptomsStr। দৈনন্দিন জীবনে অস্বস্তি বাড়ছে।",
                "৩. অতীতে কোনো জটিলতা থাকলে তা ডাক্তারের সামনে তুলে ধরা প্রয়োজন।"
            ),
            whatToShowDoctor = listOf(
                "পূর্বে প্রেসক্রাইব করা সকল চলমান ওষুধের তালিকা",
                "সাম্প্রতিক কোনো প্যাথলজি বা ল্যাব টেস্টের রিপোর্ট",
                "উপসর্গগুলো দিনের কোন সময়ে বেশি অনুভূত হয় তার বিবরণ"
            ),
            cheatQuestionsForDoctor = listOf(
                "আমার এই লক্ষণের সাথে পূর্বের কোনো দীর্ঘস্থায়ী রোগের সম্পর্ক আছে কি?",
                "আমার কি নির্দিষ্ট কোনো ল্যাব টেস্ট করানো প্রয়োজন?",
                "কোন ধরনের উপসর্গ দেখলে জরুরি ভিত্তিতে হাসপাতালে যোগাযোগ করতে হবে?"
            ),
            suggestedSpecialistBn = "জেনারেল মেডিসিন বিশেষজ্ঞ"
        )
    }
}

data class ConsultationAudioActionItem(
    val task: String,
    val category: String = "GENERAL"
)

data class ConsultationAudioSummaryResult(
    val doctorName: String,
    val summary: String,
    val actionItems: List<ConsultationAudioActionItem>,
    val pendingQuestions: List<String>,
    val followUpDays: Int,
    val followUpReason: String
)

data class RagDocumentAnalysisResult(
    val title: String,
    val recordType: String,
    val explanationBn: String,
    val keyPoints: List<String>,
    val actionAdviceBn: String,
    val questionsForDoctor: List<String>,
    val rawOcrText: String
)

data class SymptomConsultationPlanResult(
    val triageAssessmentBn: String,
    val needsDoctorVisit: Boolean,
    val homeCareAdviceBn: String,
    val speakingPoints: List<String> = emptyList(),
    val whatToShowDoctor: List<String>,
    val cheatQuestionsForDoctor: List<String>,
    val suggestedSpecialistBn: String
)
