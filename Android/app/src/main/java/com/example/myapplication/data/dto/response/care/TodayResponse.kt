package com.example.myapplication.data.dto.response.care

data class TodayResponse(
    val clientId: Long,
    val today: Long,
    val bodyComposition: BodyCompositionDto?,
    val stress: StressDto?,
    val heartRate: HeartRateDto?,
    val bloodPressure: BloodPressureDto?
)

data class BodyCompositionDto(
    val compositionId: Long,
    val bfp: ValueLevel,
    val bmr: ValueLevel,
    val ecf: ValueLevel
)

data class StressDto(
    val stressId: Long,
    val stressValue: ValueLevel,
    val stressLevel: String
)

data class HeartRateDto(
    val heartId: Long,
    val bpm: ValueLevel,
    val oxygen: ValueLevel
)

data class BloodPressureDto(
    val bloodId: Long,
    val sbp: ValueLevel,
    val dbp: ValueLevel
)
