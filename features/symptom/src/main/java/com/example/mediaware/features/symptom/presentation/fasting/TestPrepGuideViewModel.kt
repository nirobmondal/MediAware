package com.example.mediaware.features.symptom.presentation.fasting

import com.example.mediaware.core.common.base.BaseViewModel
import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.symptom.domain.model.FastingGuideline
import com.example.mediaware.features.symptom.domain.repository.FastingAlarmScheduler
import com.example.mediaware.features.symptom.domain.usecase.ScheduleFastingAlarmUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TestPrepGuideViewModel @Inject constructor(
    private val scheduleFastingAlarmUseCase: ScheduleFastingAlarmUseCase,
    private val fastingAlarmScheduler: FastingAlarmScheduler
) : BaseViewModel<TestPrepGuideUiState, TestPrepGuideUiEvent, TestPrepGuideSideEffect>(
    TestPrepGuideUiState(availableGuidelines = DEFAULT_GUIDELINES)
) {

    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onEvent(event: TestPrepGuideUiEvent) {
        when (event) {
            is TestPrepGuideUiEvent.OnSelectTest -> {
                setState { copy(selectedTestId = event.testId) }
            }
            is TestPrepGuideUiEvent.OnScheduleFastingAlarm -> {
                scheduleAlarm(event.fastingHours)
            }
            TestPrepGuideUiEvent.OnCancelAlarm -> {
                val currentTest = uiState.value.availableGuidelines.firstOrNull { it.testId == uiState.value.selectedTestId }
                currentTest?.let { fastingAlarmScheduler.cancelFastingAlarm(it.testNameBn) }
                setState { copy(isAlarmScheduled = false, scheduledTimeFormattedBn = null) }
                sendEffect(TestPrepGuideSideEffect.ShowToast("ফাস্টিং অ্যালার্ম বাতিল করা হয়েছে"))
            }
        }
    }

    private fun scheduleAlarm(hours: Int) {
        val currentTest = uiState.value.availableGuidelines.firstOrNull { it.testId == uiState.value.selectedTestId }
            ?: return

        when (val result = scheduleFastingAlarmUseCase(testNameBn = currentTest.testNameBn, fastingHours = hours)) {
            is Resource.Success -> {
                val formattedTime = timeFormat.format(Date(result.data)).toBengaliDigits()
                setState {
                    copy(
                        isAlarmScheduled = true,
                        scheduledTimeFormattedBn = formattedTime,
                        scheduledHours = hours
                    )
                }
                sendEffect(TestPrepGuideSideEffect.ShowToast("${currentTest.testNameBn} এর জন্য ফাস্টিং অ্যালার্ম নির্ধারিত হয়েছে ($formattedTime)"))
            }
            is Resource.Error -> {
                sendEffect(TestPrepGuideSideEffect.ShowToast(result.messageBn))
            }
            else -> {}
        }
    }

    companion object {
        val DEFAULT_GUIDELINES = listOf(
            FastingGuideline(
                testId = "fbs",
                testNameBn = "ফাস্টিং ব্লাড সুগার (FBS)",
                testNameEn = "Fasting Blood Sugar",
                recommendedFastingHours = 8,
                waterPermitted = true,
                guidancePointsBn = listOf(
                    "পরীক্ষার পূর্বে কমপক্ষে ৮ থেকে ১০ ঘণ্টা কোনো খাবার গ্রহণ করা যাবে না।",
                    "তৃষ্ণা পেলে সামান্য সাধারণ পানি পান করা সম্পূর্ণ অনুমোদিত।",
                    "সকালে রক্ত দেওয়ার আগ পর্যন্ত চা, কফি, পান বা সিগারেট গ্রহণ থেকে বিরত থাকুন।",
                    "ডায়াবেটিসের সকালের ওষুধ বা ইনসুলিন রক্ত দেওয়ার পরেই গ্রহণ করবেন।"
                ),
                eveningAlertTextBn = "রাত ১০টার পর পানি ছাড়া আর কিছু খাবেন না। সকালে রক্ত দিয়ে নাস্তা করবেন।"
            ),
            FastingGuideline(
                testId = "lipid",
                testNameBn = "লিপিড প্রোফাইল (Lipid Profile)",
                testNameEn = "Lipid Profile / Cholesterol",
                recommendedFastingHours = 10,
                waterPermitted = true,
                guidancePointsBn = listOf(
                    "সঠিক ফলাফলের জন্য ১০ থেকে ১২ ঘণ্টা অভুক্ত (ফাস্টিং) থাকা প্রয়োজন।",
                    "পরীক্ষার আগের দিন রাতে অতিরিক্ত তৈলাক্ত বা গুরুপাক খাবার এড়িয়ে চলুন।",
                    "পর্যাপ্ত সাধারণ পানি পান করা যাবে। কোনো কোমল পানীয় খাওয়া যাবে না।"
                ),
                eveningAlertTextBn = "রাত ৯:৩০ এর মধ্যে রাতের খাবার শেষ করুন। পরীক্ষা সম্পন্ন না হওয়া পর্যন্ত অভুক্ত থাকুন।"
            ),
            FastingGuideline(
                testId = "usg",
                testNameBn = "আল্ট্রাসনোগ্রাম (হোল অ্যাবডোমেন)",
                testNameEn = "USG of Whole Abdomen",
                recommendedFastingHours = 6,
                waterPermitted = true,
                guidancePointsBn = listOf(
                    "পেটের আল্ট্রাসনোগ্রামের জন্য কমপক্ষে ৬ ঘণ্টা পেট খালি রাখতে হয় যাতে গ্যাস কম থাকে।",
                    "লোয়ার অ্যাবডোমেন বা প্রস্রাবের থলি পরিষ্কার দেখতে পরীক্ষার ১ ঘণ্টা আগে প্রচুর পানি পান করে প্রস্রাবের বেগ ধরে রাখতে হবে।"
                ),
                eveningAlertTextBn = "পরীক্ষার ৬ ঘণ্টা আগে হালকা খাবার খান। পরীক্ষার পূর্বে পানি পান করে বেগ রাখুন।"
            )
        )
    }
}
