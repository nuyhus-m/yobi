package com.example.myapplication.data.dto.response.visitlog

data class DailyLogsListDTO(
    val scheduleId : Int,
    val clientName : String,
    val visitedDate : Long
)
