package com.example.myapplication.data.dto.response.schedule

data class PeriodScheduleResponse(
    val scheduleId: Int,
    val clientId: Int,
    val clientName: String,
    val visitedDate: Long,
    val startAt: Long,
    val endAt: Long
)
