package com.example.myapplication.data.dto.response.visitlog

data class ReportsResponse(
    val reports: List<Report>
)

data class Report(
    val reportId: Long,
    val createdAt: Long
)
