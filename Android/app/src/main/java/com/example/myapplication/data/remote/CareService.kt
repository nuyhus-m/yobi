package com.example.myapplication.data.remote

import com.example.myapplication.data.dto.request.care.HealthResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CareService {

    @GET("dashboard/{clientId}/{userId}/total-health")
    suspend fun getTotalHealth(
        @Path("clientId") clientId: Int,
        @Path("userId") userId: Int,
        @Query("size") size: Int,
        @Query("cursorDate") cursorDate: String? = null
    ): Response<HealthResponse>
}