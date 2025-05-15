package com.example.myapplication.data.repository

import com.example.myapplication.data.dto.request.visitlog.ContentRequest
import com.example.myapplication.data.dto.response.visitlog.DailyHumanDTO
import com.example.myapplication.data.dto.response.visitlog.DailyLogResponse
import com.example.myapplication.data.dto.response.visitlog.DailyLogsListResponse
import com.example.myapplication.data.remote.DailyService
import retrofit2.Response
import javax.inject.Inject

class DailyRepository @Inject constructor(
    private val dailyService: DailyService
) {


    suspend fun getDailyHumanList(): Response<List<DailyHumanDTO>> {
        return dailyService.getDailyHumanList()
    }

    suspend fun getDailyLogs(scheduleId: Int): Response<DailyLogResponse> {
        return dailyService.getDailyLogs(scheduleId)
    }

    suspend fun getDailyLogsList(clientId: Int): Response<DailyLogsListResponse> {
        return dailyService.getDailyLogsList(clientId)
    }


    suspend fun updateDailyLog(scheduleId: Int, content: String): Response<Unit> {
        val request = ContentRequest(content = content)
        return dailyService.updateDailyLog(scheduleId, request)
    }

    suspend fun patchDailyLogsDelete(scheduleId: Int) : Response<Unit>{
        return dailyService.patchDailyLogsDelete(scheduleId)
    }

}