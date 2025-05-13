package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.response.schedule.DayScheduleResponse
import com.example.myapplication.data.dto.response.schedule.PeriodScheduleResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ScheduleService  {

    @GET("schedules/period")
    suspend fun getPeriodSchedule(
        @Query("start") start: Long,
        @Query("end") end:Long
    ): Response<List<PeriodScheduleResponse>>

    @GET("schedules/day")
    suspend fun getDaySchedule(
        @Query("date") date:Long
    ): Response<List<DayScheduleResponse>>


}