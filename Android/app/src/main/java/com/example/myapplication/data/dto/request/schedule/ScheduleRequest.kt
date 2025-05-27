package com.example.myapplication.data.dto.request.schedule

data class ScheduleRequest(
    val clientId: Int,
    val visitedDate: Long,
    val startAt: Long,
    val endAt: Long
)
