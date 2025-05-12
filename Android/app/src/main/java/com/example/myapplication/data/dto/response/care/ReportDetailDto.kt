package com.example.myapplication.data.dto.response.care

data class ReportDetailDto(
    val reportId: Int,
    val report_content: String,
    val log_summary: String,
    val createdAt: Long
)
