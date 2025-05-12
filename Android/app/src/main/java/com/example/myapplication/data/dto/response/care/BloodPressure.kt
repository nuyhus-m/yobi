package com.example.myapplication.data.dto.response.care

data class BloodPressure(
    val sbp: List<MetricData>,
    val dbp: List<MetricData>
)