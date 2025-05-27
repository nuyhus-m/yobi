package com.example.myapplication.data.dto.response.care

data class BodyComposition(
    val bfp: List<MetricData>,
    val bmr: List<MetricData>,
    val ecf: List<MetricData>,
    val protein: List<MetricData>
)