package com.example.myapplication.data.dto.response.care

data class ReportDetailDto(
    val reportId: Long,
    val reportContent: String,
    val logSummery: String,
    val createdAt: Long
)
