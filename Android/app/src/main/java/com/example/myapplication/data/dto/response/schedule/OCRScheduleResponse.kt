package com.example.myapplication.data.dto.response.schedule

data class OCRScheduleResponse(
    val schedules: List<OCRScheduleItem>,
    val formMatch: Boolean,
    val whichDay: Int
)

data class OCRScheduleItem(
    val dateTimestamp: Long,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val clientName: String
)
