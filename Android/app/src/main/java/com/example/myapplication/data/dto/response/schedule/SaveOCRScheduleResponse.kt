package com.example.myapplication.data.dto.response.schedule

data class SaveOCRScheduleResponse(
    val successCount: Int,
    val failCount: Int,
    val failureReasons: List<String>
)
