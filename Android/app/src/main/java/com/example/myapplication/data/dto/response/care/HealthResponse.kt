package com.example.myapplication.data.dto.response.care

data class HealthResponse(
    val clientId: Int,
    val bodyComposition: BodyComposition,
    val bloodPressure: BloodPressure,
    val stress: Stress
)