package com.example.myapplication.data.dto.response.measure

data class HeartRateResultResponse(
    val bpm: Bpm,
    val heartId: Int,
    val oxygen: Oxygen
)