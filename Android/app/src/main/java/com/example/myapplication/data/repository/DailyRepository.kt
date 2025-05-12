package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.response.visitlog.DailyHumanListResponse
import com.example.myapplication.data.dto.response.visitlog.DailyLogResponse
import com.example.myapplication.data.dto.response.visitlog.DailyLogsListResponse
import com.example.myapplication.data.remote.DailyService
import retrofit2.Response
import javax.inject.Inject

class DailyRepository @Inject constructor(
    private val dailyService: DailyService
) {

    suspend fun getDailyLogs(scheduleId: Int): Response<DailyLogResponse> {
        return dailyService.getDailyLogs(scheduleId)
    }

    suspend fun getDailyLogsList(clientId: Int): Response<DailyLogsListResponse> {
        return dailyService.getDailyLogsList(clientId)
    }

    suspend fun getDailyHumanList(): Response<DailyHumanListResponse> {
        return dailyService.getDailyHumanList()
    }

    suspend fun patchDailyLogs(scheduleId: Int, body: String): Response<Unit> {
        return dailyService.patchDailyLogs(scheduleId, body)
    }

    suspend fun patchDailyLogsDelete(scheduleId: Int) : Response<Unit>{
        return dailyService.patchDailyLogsDelete(scheduleId)
    }

}