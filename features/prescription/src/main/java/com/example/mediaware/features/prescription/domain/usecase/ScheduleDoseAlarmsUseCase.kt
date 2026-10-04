package com.example.mediaware.features.prescription.domain.usecase

import com.example.mediaware.features.prescription.domain.model.SlotSchedule
import com.example.mediaware.features.prescription.domain.repository.DoseAlarmScheduler
import javax.inject.Inject

class ScheduleDoseAlarmsUseCase @Inject constructor(
    private val scheduler: DoseAlarmScheduler
) {
    operator fun invoke(schedules: List<SlotSchedule>): Int {
        var scheduledCount = 0

        for (schedule in schedules) {
            if (schedule.isEnabled && schedule.medicines.isNotEmpty()) {
                val medNames = schedule.medicines.joinToString(", ") { med ->
                    "${med.brandName} (${med.strength})"
                }

                val titleBn = "${schedule.slot.labelBn}র ওষুধ গ্রহণের সময়"
                val instructionBn = "$medNames সেবন করুন।"
                val reminderId = "dose_slot_${schedule.slot.name.lowercase()}"

                scheduler.scheduleExactDailyAlarm(
                    reminderId = reminderId,
                    hour = schedule.hour,
                    minute = schedule.minute,
                    titleBn = titleBn,
                    instructionBn = instructionBn
                )
                scheduledCount++
            } else if (!schedule.isEnabled) {
                val reminderId = "dose_slot_${schedule.slot.name.lowercase()}"
                scheduler.cancelAlarm(reminderId)
            }
        }

        return scheduledCount
    }
}
