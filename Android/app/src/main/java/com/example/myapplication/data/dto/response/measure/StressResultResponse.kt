package com.example.myapplication.data.dto.response.measure

data class StressResultResponse(
    val stressId: Int,
    val stressLevel: String,
    val stressValue: StressValue
) : HealthDataResultResponse()