package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.response.visitlog.DailyHumanListResponse
import com.example.myapplication.data.dto.response.visitlog.DailyLogResponse
import com.example.myapplication.data.dto.response.visitlog.DailyLogsListResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface DailyService {

    // 일지 단건 조회
    @GET("dailylogs/{scheduleId}")
    suspend fun getDailyLogs(
        @Path("scheduleId") scheduleId: Int
    ): Response<DailyLogResponse>

    // 특정 돌봄 대상에 대한 일지 리스트
    @GET("clients/{clientId}/dailylogs")
    suspend fun getDailyLogsList(
        @Path("clientId") clientId: Int
    ): Response<DailyLogsListResponse>

    // 사용자의 일지 전체 리스트
    @GET("dailylogs")
    suspend fun getDailyHumanList(): Response<DailyHumanListResponse>

    // 일지 작성 및 수정
    @PATCH("dailylogs/{scheduleId}")
    suspend fun patchDailyLogs(
        @Path("scheduleId") scheduleId: Int,
        @Body body: String
    ): Response<Unit>

    // 일지 삭제
    @PATCH("dailylogs/{scheduleId}/delete")
    suspend fun patchDailyLogsDelete(
        @Path("scheduleId") scheduleId: Int
    ): Response<Unit>
}