package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.request.schedule.ScheduleRequest
import com.example.myapplication.data.dto.response.schedule.DayScheduleResponse
import com.example.myapplication.data.dto.response.schedule.OCRScheduleItem
import com.example.myapplication.data.dto.response.schedule.OCRScheduleResponse
import com.example.myapplication.data.dto.response.schedule.PeriodScheduleResponse
import com.example.myapplication.data.dto.response.schedule.SaveOCRScheduleResponse
import com.example.myapplication.data.dto.response.schedule.ScheduleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Part
import okhttp3.MultipartBody

interface ScheduleService  {

    @GET("schedules/period")
    suspend fun getPeriodSchedule(
        @Query("startDate") startDate: Long,
        @Query("endDate") endDate:Long
    ): Response<List<PeriodScheduleResponse>>

    @GET("schedules/day")
    suspend fun getDaySchedule(
        @Query("date") date:Long
    ): Response<List<DayScheduleResponse>>

    @POST("schedules")
    suspend fun postSchedule(@Body request: ScheduleRequest): Response<Unit>

    @PATCH("schedules/{scheduleId}")
    suspend fun editSchedule(
        @Path("scheduleId") scheduleId: Int,
        @Body request: ScheduleRequest
    ): Response<Unit>

    @GET("schedules/{scheduleId}")
    suspend fun getSchedule(@Path("scheduleId") scheduleId: Int): Response<ScheduleResponse>

    @DELETE("schedules/{scheduleId}")
    suspend fun deleteSchedule(@Path("scheduleId") scheduleId: Int): Response<Unit>

    @Multipart
    @POST("schedules/ocr/analyze")
    suspend fun registerPhotoSchedule(
        @Part image: MultipartBody.Part,
        @Query("year") year: Int,
        @Query("month") month: Int,
        @Query("timezone") timezone: String = "Asia/Seoul"
    ): Response<OCRScheduleResponse>

    @POST("schedules/ocr/save")
    suspend fun savePhotoSchedule(
        @Body request: List<OCRScheduleItem>
    ): Response<SaveOCRScheduleResponse>
    
}