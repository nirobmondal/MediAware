package com.example.mediaware.features.chamber.domain.model

import java.util.UUID

data class DoctorQuestionItem(
    val id: String = UUID.randomUUID().toString(),
    val questionBn: String,
    val categoryBn: String = "সাধারণ আলোচনা",
    val isDiscussed: Boolean = false
)

data class LabSummaryItem(
    val testName: String,
    val valueWithUnitBn: String,
    val statusTag: String, // "উচ্চ", "সতর্ক", "স্বাভাবিক"
    val statusColorHex: Long // e.g. 0xFFBA1A1A, 0xFFE65100, 0xFF006A6A
)

data class PatientPresentationSummary(
    val patientNameBn: String,
    val ageYears: Int,
    val genderBn: String,
    val bloodGroup: String,
    val chronicConditionsBn: String,
    val chiefComplaintsBn: String,
    val complaintDurationBn: String,
    val recentLabs: List<LabSummaryItem>,
    val trendObservationBn: String,
    val currentMedicinesBn: String
)
