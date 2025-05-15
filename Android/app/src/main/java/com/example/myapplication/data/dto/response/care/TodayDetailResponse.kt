package com.example.myapplication.data.dto.response.care

data class TodayDetailResponse(
    val clientId: Int,
    val today: Long,
    val bodyComposition: TodayBodyComposition?,
    val temperature: Temperature?,
    val bloodPressure: TodayBloodPressure?,
    val heartRate: HeartRate?,
    val stress: TodayStress?
)

data class TodayBodyComposition(
    val compositionId: Int,
    val bfp: ValueLevel,
    val bfm: ValueLevel,
    val smm: ValueLevel,
    val bmr: ValueLevel,
    val ecf: ValueLevel,
    val protein: ValueLevel,
    val mineral: ValueLevel,
    val bodyAge: Int
)

data class Temperature(
    val temperatureId: Int,
    val temperature: ValueLevel
)

data class TodayBloodPressure(
    val bloodId: Int,
    val sbp: ValueLevel,
    val dbp: ValueLevel
)

data class HeartRate(
    val heartId: Int,
    val bpm: ValueLevel,
    val oxygen: ValueLevel
)

data class TodayStress(
    val stressId: Int,
    val stressValue: ValueLevel,
    val stressLevel: String
)
