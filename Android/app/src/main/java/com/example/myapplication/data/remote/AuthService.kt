package com.example.myapplication.data.remote

import retrofit2.http.Body
import retrofit2.http.POST
import com.example.myapplication.data.dto.request.mypage.LoginRequest
import com.example.myapplication.data.dto.response.mypage.LoginResponse
import retrofit2.Response

interface AuthService {

    @POST("users/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}