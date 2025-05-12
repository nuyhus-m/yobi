package com.example.myapplication.data.dto.request.care

data class BloodPressure(
    val sbp: List<MetricData>,
    val dbp: List<MetricData>
)