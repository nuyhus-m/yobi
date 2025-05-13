package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.response.schedule.DayScheduleResponse
import com.example.myapplication.data.dto.response.schedule.PeriodScheduleResponse
import com.example.myapplication.data.remote.ScheduleService
import jakarta.inject.Inject
import retrofit2.Response

class ScheduleRepository @Inject constructor(
    private val scheduleService: ScheduleService
){
    suspend fun getPeriodSchedule(start: Long, end:Long): Response<List<PeriodScheduleResponse>> {
        return scheduleService.getPeriodSchedule(start, end)
    }

    suspend fun getDaySchedule(date:Long): Response<List<DayScheduleResponse>> {
        return scheduleService.getDaySchedule(date)
    }
}