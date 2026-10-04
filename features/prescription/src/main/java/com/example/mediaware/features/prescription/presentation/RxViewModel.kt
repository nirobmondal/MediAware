package com.example.mediaware.features.prescription.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mediaware.core.common.result.Resource
import com.example.mediaware.core.designsystem.util.toBengaliDigits
import com.example.mediaware.features.prescription.domain.model.DoseSlot
import com.example.mediaware.features.prescription.domain.model.MedicineExplanation
import com.example.mediaware.features.prescription.domain.model.PrescriptionItem
import com.example.mediaware.features.prescription.domain.model.SlotSchedule
import com.example.mediaware.features.prescription.domain.usecase.DecodeLatinRxUseCase
import com.example.mediaware.features.prescription.domain.usecase.DosageDecoder
import com.example.mediaware.features.prescription.domain.usecase.GetMedicineInfoUseCase
import com.example.mediaware.features.prescription.domain.usecase.ScheduleDoseAlarmsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RxUiState(
    val isLoading: Boolean = false,
    val capturedImageUri: Uri? = null,
    val items: List<PrescriptionItem> = emptyList(),
    val explanations: List<MedicineExplanation> = emptyList(),
    val schedules: List<SlotSchedule> = emptyList(),
    val searchQuery: String = "",
    val searchSuggestions: List<String> = emptyList(),
    val editingItem: PrescriptionItem? = null,
    val isAddingNew: Boolean = false,
    val errorMessageBn: String? = null
)

sealed interface RxUiEvent {
    data class OnImageCaptured(val uri: Uri?) : RxUiEvent
    data object OnUseDemoSample : RxUiEvent
    data class OnVoiceInputSubmitted(val spokenText: String) : RxUiEvent
    data class OnSearchQueryChanged(val query: String) : RxUiEvent
    data class OnSelectSearchSuggestion(val drugName: String) : RxUiEvent
    data class OnStartEdit(val item: PrescriptionItem) : RxUiEvent
    data object OnStartAdd : RxUiEvent
    data class OnSaveEdit(val updatedItem: PrescriptionItem) : RxUiEvent
    data class OnSaveNew(val brandName: String, val dosePattern: String, val mealTiming: String, val durationDays: Int) : RxUiEvent
    data object OnDismissDialog : RxUiEvent
    data class OnDeleteItem(val id: String) : RxUiEvent
    data object OnLoadExplanations : RxUiEvent
    data class OnToggleSlot(val slot: DoseSlot, val isEnabled: Boolean) : RxUiEvent
    data class OnUpdateSlotTime(val slot: DoseSlot, val hour: Int, val minute: Int) : RxUiEvent
    data object OnSaveScheduleAndAlarms : RxUiEvent
    data object OnClearError : RxUiEvent
}

sealed interface RxUiSideEffect {
    data object NavigateToVerify : RxUiSideEffect
    data object NavigateToExplain : RxUiSideEffect
    data object NavigateToSchedule : RxUiSideEffect
    data object NavigateToHome : RxUiSideEffect
    data class ShowToast(val messageBn: String) : RxUiSideEffect
}

@HiltViewModel
class RxViewModel @Inject constructor(
    private val decodeLatinRxUseCase: DecodeLatinRxUseCase,
    private val getMedicineInfoUseCase: GetMedicineInfoUseCase,
    private val scheduleDoseAlarmsUseCase: ScheduleDoseAlarmsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RxUiState())
    val uiState: StateFlow<RxUiState> = _uiState.asStateFlow()

    private val _sideEffects = Channel<RxUiSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    init {
        // Initialize default slot schedules
        val defaultSchedules = listOf(
            SlotSchedule(
                slot = DoseSlot.MORNING,
                timeStringBn = "সকাল ০৮:০০",
                hour = DoseSlot.MORNING.defaultHour,
                minute = DoseSlot.MORNING.defaultMinute,
                medicines = emptyList(),
                isEnabled = true
            ),
            SlotSchedule(
                slot = DoseSlot.NOON,
                timeStringBn = "দুপুর ০১:৩০",
                hour = DoseSlot.NOON.defaultHour,
                minute = DoseSlot.NOON.defaultMinute,
                medicines = emptyList(),
                isEnabled = true
            ),
            SlotSchedule(
                slot = DoseSlot.NIGHT,
                timeStringBn = "রাত ০৮:৩০",
                hour = DoseSlot.NIGHT.defaultHour,
                minute = DoseSlot.NIGHT.defaultMinute,
                medicines = emptyList(),
                isEnabled = true
            )
        )
        _uiState.update { it.copy(schedules = defaultSchedules) }
    }

    fun onEvent(event: RxUiEvent) {
        when (event) {
            is RxUiEvent.OnImageCaptured -> {
                _uiState.update { it.copy(isLoading = true, capturedImageUri = event.uri) }
                viewModelScope.launch {
                    val demoItems = decodeLatinRxUseCase.getDemoPrescriptionItems()
                    _uiState.update { it.copy(isLoading = false, items = demoItems) }
                    _sideEffects.send(RxUiSideEffect.NavigateToVerify)
                }
            }

            is RxUiEvent.OnUseDemoSample -> {
                val demoItems = decodeLatinRxUseCase.getDemoPrescriptionItems()
                _uiState.update { it.copy(items = demoItems) }
                viewModelScope.launch {
                    _sideEffects.send(RxUiSideEffect.NavigateToVerify)
                }
            }

            is RxUiEvent.OnVoiceInputSubmitted -> {
                val parsed = decodeLatinRxUseCase.parseLine(event.spokenText)
                if (parsed != null) {
                    _uiState.update { it.copy(items = it.items + parsed) }
                    viewModelScope.launch {
                        _sideEffects.send(RxUiSideEffect.NavigateToVerify)
                    }
                } else {
                    val fallback = PrescriptionItem(
                        brandName = event.spokenText.trim(),
                        genericName = event.spokenText.trim()
                    )
                    _uiState.update { it.copy(items = it.items + fallback) }
                    viewModelScope.launch {
                        _sideEffects.send(RxUiSideEffect.NavigateToVerify)
                    }
                }
            }

            is RxUiEvent.OnSearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
            }

            is RxUiEvent.OnSelectSearchSuggestion -> {
                val item = PrescriptionItem(
                    brandName = event.drugName,
                    genericName = event.drugName
                )
                _uiState.update { it.copy(items = it.items + item, searchQuery = "") }
            }

            is RxUiEvent.OnStartEdit -> {
                _uiState.update { it.copy(editingItem = event.item) }
            }

            is RxUiEvent.OnStartAdd -> {
                _uiState.update { it.copy(isAddingNew = true) }
            }

            is RxUiEvent.OnDismissDialog -> {
                _uiState.update { it.copy(editingItem = null, isAddingNew = false) }
            }

            is RxUiEvent.OnSaveEdit -> {
                val updated = event.updatedItem
                val instruction = DosageDecoder.decodeSchedule(updated.rawDosagePattern, updated.rawMealTiming)
                val fullItem = updated.copy(
                    morningQty = instruction.morningQty,
                    noonQty = instruction.noonQty,
                    nightQty = instruction.nightQty,
                    timingSlotBn = instruction.timingSlotBn,
                    mealInstructionBn = instruction.mealInstructionBn
                )

                _uiState.update { state ->
                    state.copy(
                        items = state.items.map { if (it.id == fullItem.id) fullItem else it },
                        editingItem = null
                    )
                }
            }

            is RxUiEvent.OnSaveNew -> {
                val instruction = DosageDecoder.decodeSchedule(event.dosePattern, event.mealTiming)
                val newItem = PrescriptionItem(
                    id = UUID.randomUUID().toString(),
                    brandName = event.brandName,
                    genericName = event.brandName,
                    rawDosagePattern = event.dosePattern,
                    rawMealTiming = event.mealTiming,
                    durationDays = event.durationDays,
                    morningQty = instruction.morningQty,
                    noonQty = instruction.noonQty,
                    nightQty = instruction.nightQty,
                    timingSlotBn = instruction.timingSlotBn,
                    mealInstructionBn = instruction.mealInstructionBn
                )
                _uiState.update { it.copy(items = it.items + newItem, isAddingNew = false) }
            }

            is RxUiEvent.OnDeleteItem -> {
                _uiState.update { it.copy(items = it.items.filterNot { item -> item.id == event.id }) }
            }

            is RxUiEvent.OnLoadExplanations -> {
                loadExplanations()
            }

            is RxUiEvent.OnToggleSlot -> {
                _uiState.update { state ->
                    state.copy(
                        schedules = state.schedules.map {
                            if (it.slot == event.slot) it.copy(isEnabled = event.isEnabled) else it
                        }
                    )
                }
            }

            is RxUiEvent.OnUpdateSlotTime -> {
                val timeBn = formatBengaliTime(event.slot.labelBn, event.hour, event.minute)
                _uiState.update { state ->
                    state.copy(
                        schedules = state.schedules.map {
                            if (it.slot == event.slot) {
                                it.copy(
                                    hour = event.hour,
                                    minute = event.minute,
                                    timeStringBn = timeBn
                                )
                            } else it
                        }
                    )
                }
            }

            is RxUiEvent.OnSaveScheduleAndAlarms -> {
                val count = scheduleDoseAlarmsUseCase(_uiState.value.schedules)
                viewModelScope.launch {
                    _sideEffects.send(
                        RxUiSideEffect.ShowToast(
                            "${count.toString().toBengaliDigits()}টি দৈনিক ওষুধের অ্যালার্ম সফলভাবে সেট করা হয়েছে।"
                        )
                    )
                    _sideEffects.send(RxUiSideEffect.NavigateToHome)
                }
            }

            is RxUiEvent.OnClearError -> {
                _uiState.update { it.copy(errorMessageBn = null) }
            }
        }
    }

    private fun loadExplanations() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val items = _uiState.value.items
            val explanationsList = mutableListOf<MedicineExplanation>()

            for (item in items) {
                val queryKey = if (item.genericName.isNotBlank()) item.genericName else item.brandName
                when (val result = getMedicineInfoUseCase(queryKey)) {
                    is Resource.Success -> {
                        val expl = result.data?.copy(brandName = item.brandName)
                        if (expl != null) {
                            explanationsList.add(expl)
                        }
                    }
                    else -> {
                        // Fallback explanation if lookup encounters error
                        explanationsList.add(
                            MedicineExplanation(
                                genericName = item.genericName.ifBlank { item.brandName },
                                brandName = item.brandName,
                                banglaName = item.brandName,
                                therapeuticClass = "প্রেসক্রিপশন মেডিসিন",
                                primaryPurposeBn = "শারীরিক সুস্থতার জন্য বিশেষজ্ঞ চিকিৎসকের পরামর্শে নির্দেশিত।",
                                standardDosageBn = "${item.timingSlotBn}, ${item.mealInstructionBn}",
                                sideEffectsBn = "কোনো পার্শ্বপ্রতিক্রিয়া দেখা দিলে চিকিৎসকের সাথে কথা বলুন।",
                                criticalWarningsBn = "খালি পেটে খাবেন না। ডাক্তারের পরামর্শ ছাড়া মাত্রা পরিবর্তন করবেন না।",
                                lifestylePrecautionsBn = "পর্যাপ্ত পানি পান করুন।",
                                isFromCache = false
                            )
                        )
                    }
                }
            }

            // Distribute items into morning, noon, and night slots
            val morningMeds = items.filter { it.morningQty > 0 }
            val noonMeds = items.filter { it.noonQty > 0 }
            val nightMeds = items.filter { it.nightQty > 0 }

            val updatedSchedules = _uiState.value.schedules.map { sched ->
                when (sched.slot) {
                    DoseSlot.MORNING -> sched.copy(medicines = morningMeds)
                    DoseSlot.NOON -> sched.copy(medicines = noonMeds)
                    DoseSlot.NIGHT -> sched.copy(medicines = nightMeds)
                }
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    explanations = explanationsList,
                    schedules = updatedSchedules
                )
            }
            _sideEffects.send(RxUiSideEffect.NavigateToExplain)
        }
    }

    private fun formatBengaliTime(label: String, hour: Int, minute: Int): String {
        val h12 = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val minStr = if (minute < 10) "0$minute" else "$minute"
        val hourBn = h12.toString().toBengaliDigits()
        val minBn = minStr.toBengaliDigits()
        return "$label $hourBn:$minBn"
    }
}
