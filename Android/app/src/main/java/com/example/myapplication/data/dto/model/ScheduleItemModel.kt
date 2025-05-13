package com.example.myapplication.data.dto.model

data class ScheduleItemModel(
    val scheduleId: Int,
    val clientId: Int,
    val clientName: String,
    val visitedDate: Long,
    val date: String, // 예: "2025-05-01"
    val timeRange: String, // 예: "10:00 ~ 11:00"
    val hasLogContent: Boolean = false
)
