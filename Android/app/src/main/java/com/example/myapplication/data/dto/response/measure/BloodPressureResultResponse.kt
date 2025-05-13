package com.example.myapplication.data.dto.response.measure

data class BloodPressureResultResponse(
    val bloodId: Int,
    val dbp: Dbp,
    val sbp: Sbp
)