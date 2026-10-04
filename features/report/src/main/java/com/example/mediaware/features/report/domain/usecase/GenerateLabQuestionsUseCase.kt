package com.example.mediaware.features.report.domain.usecase

import com.example.mediaware.features.report.domain.model.DoctorQuestion
import com.example.mediaware.features.report.domain.model.ExtractedLabItem
import com.example.mediaware.features.report.domain.model.LabStatus
import java.util.UUID
import javax.inject.Inject

class GenerateLabQuestionsUseCase @Inject constructor() {

    operator fun invoke(items: List<ExtractedLabItem>): List<DoctorQuestion> {
        val questions = mutableListOf<DoctorQuestion>()

        for (item in items) {
            if (item.status == LabStatus.CRITICAL || item.status == LabStatus.BORDERLINE) {
                when (item.key) {
                    "fbs", "rbs", "hba1c" -> {
                        questions.add(
                            DoctorQuestion(
                                id = UUID.randomUUID().toString(),
                                testNameBn = item.testNameBn,
                                questionBn = "আমার রক্তে সুগারের মাত্রা নিয়ন্ত্রণে রাখতে শুধুমাত্র খাদ্যাভ্যাস ও ব্যায়াম যথেষ্ট, নাকি নতুন কোনো ওষুধ শুরু করতে হবে?",
                                rationaleBn = "সুগার মাত্রা বৃদ্ধি ডায়াবেটিসের ঝুঁকি নির্দেশ করে। প্রাথমিক নিয়ন্ত্রণ জটিলতা প্রতিরোধ করে।"
                            )
                        )
                        questions.add(
                            DoctorQuestion(
                                id = UUID.randomUUID().toString(),
                                testNameBn = item.testNameBn,
                                questionBn = "কতদিন পর আমার পুনরায় ব্লাড সুগার বা এইচবিএ১সি পরীক্ষা করানো প্রয়োজন?",
                                rationaleBn = "চিকিৎসার কার্যকারিতা পর্যালোচনায় নিয়মিত ফলোআপ পরীক্ষা আবশ্যক।"
                            )
                        )
                    }

                    "creatinine" -> {
                        questions.add(
                            DoctorQuestion(
                                id = UUID.randomUUID().toString(),
                                testNameBn = item.testNameBn,
                                questionBn = "আমার কিডনির সুরক্ষার জন্য কোনো সাধারণ ব্যথানাশক বা অন্যান্য নিয়মিত ওষুধ কি বন্ধ বা পরিবর্তন করতে হবে?",
                                rationaleBn = "উচ্চ ক্রিয়েটিনিনে অপ্রয়োজনীয় ব্যথানাশক ওষুধ কিডনির উপর তীব্র চাপ তৈরি করতে পারে।"
                            )
                        )
                        questions.add(
                            DoctorQuestion(
                                id = UUID.randomUUID().toString(),
                                testNameBn = item.testNameBn,
                                questionBn = "দৈনিক পানি পানের পরিমাণ বা প্রোটিন জাতীয় খাবার গ্রহণে আমার কি কোনো সীমাবদ্ধতা রয়েছে?",
                                rationaleBn = "কিডনি কার্যক্ষমতার উপর ভিত্তি করে পানি ও প্রোটিনের সঠিক মাত্রা ঠিক করা জরুরি।"
                            )
                        )
                    }

                    "hemoglobin" -> {
                        questions.add(
                            DoctorQuestion(
                                id = UUID.randomUUID().toString(),
                                testNameBn = item.testNameBn,
                                questionBn = "হিমোগ্লোবিন বাড়াতে আমার খাদ্যাভ্যাসে কী কী বিশেষ খাবার যুক্ত করা উচিত এবং কোনো আয়রন সাপ্লিমেন্ট প্রয়োজন কি?",
                                rationaleBn = "রক্তস্বল্পতা বা অ্যানিমিয়া দূর করতে সুষম পুষ্টি ও প্রয়োজনে সাপ্লিমেন্ট সহায়তা করে।"
                            )
                        )
                    }

                    "cholesterol" -> {
                        questions.add(
                            DoctorQuestion(
                                id = UUID.randomUUID().toString(),
                                testNameBn = item.testNameBn,
                                questionBn = "রক্তে অতিরিক্ত কোলেস্টেরল কমাতে খাদ্য নিয়ন্ত্রণের পাশাপাশি কোনো লিপিড লোয়ারিং ওষুধের দরকার আছে কি?",
                                rationaleBn = "উচ্চ কোলেস্টেরল দীর্ঘমেয়াদে রক্তনালীর ব্লকেজ ও হৃদরোগের ঝুঁকি বাড়াতে পারে।"
                            )
                        )
                    }
                }
            }
        }

        // If all tests are normal or questions list is empty, supply standard preventive health questions
        if (questions.isEmpty()) {
            questions.add(
                DoctorQuestion(
                    id = UUID.randomUUID().toString(),
                    testNameBn = "সার্বিক রিপোর্ট",
                    questionBn = "আমার সাম্প্রতিক ল্যাব টেস্টের রিপোর্টগুলো কি সন্তোষজনক? ভবিষ্যতে ভালো রাখতে কোনো প্রতিরোধমূলক পরামর্শ আছে কি?",
                    rationaleBn = "সুস্বাস্থ্যের ধারাবাহিকতা ধরে রাখতে চিকিৎসকের লাইফস্টাইল নির্দেশনা গ্রহণ।"
                )
            )
            questions.add(
                DoctorQuestion(
                    id = UUID.randomUUID().toString(),
                    testNameBn = "নিয়মিত ফলোআপ",
                    questionBn = "আমার বয়স ও শারীরিক অবস্থার প্রেক্ষিতে পরবর্তী রুটিন স্বাস্থ্য পরীক্ষা কতদিন পর করানো উচিত?",
                    rationaleBn = "রুটিন স্ক্রিনিংয়ে প্রাথমিক পর্যায়ে রোগ শনাক্তকরণ সহজ হয়।"
                )
            )
        }

        return questions
    }
}
