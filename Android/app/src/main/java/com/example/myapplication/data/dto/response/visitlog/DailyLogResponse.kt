package com.example.myapplication.data.dto.response.visitlog

data class DailyLogResponse(
    val logContent: String,
    val clientName: String,
    val visitedDate: Long
)
